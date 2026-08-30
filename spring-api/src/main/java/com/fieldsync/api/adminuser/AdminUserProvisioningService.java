package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.TenantEntity;
import com.fieldsync.api.domain.entity.UserEntity;

import com.fieldsync.api.domain.repository.TenantRepository;
import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.keycloak.KeycloakAdminClient;
import com.fieldsync.api.keycloak.KeycloakAdminException;
import com.fieldsync.api.keycloak.KeycloakUserConflictException;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import java.util.Base64;


@Service
public class AdminUserProvisioningService {

    private static final SecureRandom
        SECURE_RANDOM =
            new SecureRandom();


    private final UserRepository
        userRepository;

    private final TenantRepository
        tenantRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;

    private final KeycloakAdminClient
        keycloakAdminClient;

    private final boolean
        createTemporaryPassword;


    public AdminUserProvisioningService(

            UserRepository userRepository,

            TenantRepository tenantRepository,

            CurrentUserService currentUserService,

            TenantContextExecutor tenantContextExecutor,

            KeycloakAdminClient keycloakAdminClient,

            @Value(
                "${fieldsync.keycloak.admin.create-temporary-password:true}"
            )
            boolean createTemporaryPassword
    ) {

        this.userRepository =
            userRepository;

        this.tenantRepository =
            tenantRepository;

        this.currentUserService =
            currentUserService;

        this.tenantContextExecutor =
            tenantContextExecutor;

        this.keycloakAdminClient =
            keycloakAdminClient;

        this.createTemporaryPassword =
            createTemporaryPassword;
    }


    public AdminUserCreateResponse createUser(
            AdminUserCreateRequest request
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();


        if (!currentUser.isAdmin()) {

            throw new AdminUserApiException(
                HttpStatus.FORBIDDEN,
                "Admin access is required"
            );
        }


        String fullName =
            requireValue(
                request == null
                    ? null
                    : request.fullName(),
                "Full name is required"
            );


        String username =
            requireValue(
                request == null
                    ? null
                    : request.username(),
                "Username is required"
            )
            .toLowerCase();


        String email =
            requireValue(
                request == null
                    ? null
                    : request.email(),
                "Email is required for sending the account setup link"
            )
            .toLowerCase();


        boolean accessWeb =
            request != null &&
            Boolean.TRUE.equals(
                request.accessWeb()
            );


        boolean accessMobile =
            request != null &&
            Boolean.TRUE.equals(
                request.accessMobile()
            );


        if (
            !accessWeb &&
            !accessMobile
        ) {

            throw new AdminUserApiException(
                HttpStatus.BAD_REQUEST,
                "Select at least one access type: Web app, Mobile app, or both"
            );
        }


        Integer tenantId =
            currentUser.tenantId();


        boolean alreadyExists =
            tenantContextExecutor.execute(
                tenantId,
                () ->
                    userRepository
                        .findByUsernameIgnoreCase(
                            username
                        )
                        .isPresent()

                    ||

                    userRepository
                        .findByEmailIgnoreCase(
                            email
                        )
                        .isPresent()
            );


        if (alreadyExists) {

            throw new AdminUserApiException(
                HttpStatus.CONFLICT,
                "Username or email is already registered"
            );
        }


        String keycloakUserId =
            null;


        try {

            keycloakUserId =
                keycloakAdminClient
                    .createUser(
                        username,
                        email,
                        fullName,
                        accessWeb,
                        accessMobile
                    );


            String temporaryPassword =
                null;


            if (createTemporaryPassword) {

                temporaryPassword =
                    generateTemporaryPassword();


                keycloakAdminClient
                    .setTemporaryPassword(
                        keycloakUserId,
                        temporaryPassword
                    );
            }


            String finalKeycloakUserId =
                keycloakUserId;


            AdminUserResponse user =
                tenantContextExecutor.execute(
                    tenantId,
                    () -> {

                        if (
                            userRepository
                                .findByUsernameIgnoreCase(
                                    username
                                )
                                .isPresent()

                            ||

                            userRepository
                                .findByEmailIgnoreCase(
                                    email
                                )
                                .isPresent()
                        ) {

                            throw new AdminUserApiException(
                                HttpStatus.CONFLICT,
                                "Username or email is already registered"
                            );
                        }


                        TenantEntity tenant =
                            tenantRepository
                                .findByIdAndActiveTrue(
                                    tenantId
                                )
                                .orElseThrow(
                                    () ->
                                        new AdminUserApiException(
                                            HttpStatus.FORBIDDEN,
                                            "Tenant is not active"
                                        )
                                );


                        UserEntity createdBy =
                            userRepository
                                .findByIdAndTenant_Id(
                                    currentUser.userId(),
                                    tenantId
                                )
                                .orElseThrow(
                                    () ->
                                        new AdminUserApiException(
                                            HttpStatus.FORBIDDEN,
                                            "Authenticated admin user was not found"
                                        )
                                );


                        UserEntity created =
                            UserEntity
                                .createKeycloakUser(
                                    tenant,
                                    createdBy,
                                    username,
                                    email,
                                    fullName,
                                    accessWeb,
                                    accessMobile,
                                    finalKeycloakUserId
                                );


                        return AdminUserResponse
                            .from(
                                userRepository
                                    .saveAndFlush(
                                        created
                                    )
                            );
                    }
                );


            return new AdminUserCreateResponse(

                createTemporaryPassword
                    ? "User created in FieldSync and Keycloak. Temporary password generated."
                    : "User created successfully, but the invitation emails were not sent.",

                user,

                false,

                keycloakUserId,

                false,

                temporaryPassword,

                null,

                ""
            );

        }
        catch (
            KeycloakUserConflictException exception
        ) {

            throw new AdminUserApiException(
                HttpStatus.CONFLICT,
                exception.getMessage()
            );
        }
        catch (
            AdminUserApiException exception
        ) {

            cleanupKeycloakUser(
                keycloakUserId
            );

            throw exception;
        }
        catch (
            KeycloakAdminException exception
        ) {

            cleanupKeycloakUser(
                keycloakUserId
            );

            throw new AdminUserApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to create user"
            );
        }
        catch (
            RuntimeException exception
        ) {

            cleanupKeycloakUser(
                keycloakUserId
            );

            throw new AdminUserApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to create user"
            );
        }
    }


    private String requireValue(
            String value,
            String message
    ) {

        if (
            value == null ||
            value.trim().isEmpty()
        ) {

            throw new AdminUserApiException(
                HttpStatus.BAD_REQUEST,
                message
            );
        }

        return value.trim();
    }


    private String generateTemporaryPassword() {

        byte[] randomBytes =
            new byte[9];

        SECURE_RANDOM.nextBytes(
            randomBytes
        );


        String encoded =
            Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(
                    randomBytes
                );


        return "Fs-"
            + encoded
            + "!9";
    }


    private void cleanupKeycloakUser(
            String keycloakUserId
    ) {

        if (
            keycloakUserId == null ||
            keycloakUserId.isBlank()
        ) {
            return;
        }


        try {

            keycloakAdminClient
                .deleteUser(
                    keycloakUserId
                );

        }
        catch (
            KeycloakAdminException ignored
        ) {

            // Preserve the original provisioning failure.
        }
    }
}