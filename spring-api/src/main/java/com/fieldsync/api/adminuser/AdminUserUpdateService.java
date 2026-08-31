package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.UserEntity;

import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.keycloak.KeycloakAdminClient;
import com.fieldsync.api.keycloak.KeycloakAdminException;
import com.fieldsync.api.keycloak.KeycloakUserConflictException;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class AdminUserUpdateService {

    private final UserRepository
        userRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;

    private final KeycloakAdminClient
        keycloakAdminClient;


    public AdminUserUpdateService(
            UserRepository userRepository,
            CurrentUserService currentUserService,
            TenantContextExecutor tenantContextExecutor,
            KeycloakAdminClient keycloakAdminClient
    ) {

        this.userRepository =
            userRepository;

        this.currentUserService =
            currentUserService;

        this.tenantContextExecutor =
            tenantContextExecutor;

        this.keycloakAdminClient =
            keycloakAdminClient;
    }


    public AdminUserUpdateResponse updateUser(
            Integer userId,
            AdminUserUpdateRequest request
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();


        requireAdmin(
            currentUser
        );


        if (
            userId == null ||
            userId <= 0
        ) {

            throw new AdminUserApiException(
                HttpStatus.NOT_FOUND,
                "User not found"
            );
        }


        if (
            userId.equals(
                currentUser.userId()
            )
            &&
            request != null
            &&
            Boolean.FALSE.equals(
                request.isActive()
            )
        ) {

            throw new AdminUserApiException(
                HttpStatus.BAD_REQUEST,
                "You cannot deactivate your own admin account"
            );
        }


        Integer tenantId =
            currentUser.tenantId();


        try {

            return tenantContextExecutor.execute(
                tenantId,
                () -> {

                    UserEntity user =
                        userRepository
                            .findByIdAndTenant_Id(
                                userId,
                                tenantId
                            )
                            .orElseThrow(
                                () ->
                                    new AdminUserApiException(
                                        HttpStatus.NOT_FOUND,
                                        "User not found"
                                    )
                            );


                    if (
                        "admin".equals(
                            user.getRole()
                        )
                    ) {

                        throw new AdminUserApiException(
                            HttpStatus.BAD_REQUEST,
                            "Admin account details cannot be edited from user management"
                        );
                    }


                    String fullName =
                        normalizeOptional(
                            request == null
                                ? null
                                : request.fullName()
                        );


                    String email =
                        normalizeEmail(
                            request == null
                                ? null
                                : request.email()
                        );


                    Boolean accessWeb =
                        request == null
                            ? null
                            : request.accessWeb();


                    Boolean accessMobile =
                        request == null
                            ? null
                            : request.accessMobile();


                    Boolean isActive =
                        request == null
                            ? null
                            : request.isActive();


                    boolean finalAccessWeb =
                        accessWeb != null
                            ? accessWeb
                            : Boolean.TRUE.equals(
                                user.getAccessWeb()
                            );


                    boolean finalAccessMobile =
                        accessMobile != null
                            ? accessMobile
                            : Boolean.TRUE.equals(
                                user.getAccessMobile()
                            );


                    if (
                        !finalAccessWeb &&
                        !finalAccessMobile
                    ) {

                        throw new AdminUserApiException(
                            HttpStatus.BAD_REQUEST,
                            "User must have at least one access type"
                        );
                    }


                    if (
                        email != null &&
                        userRepository
                            .findByEmailIgnoreCaseAndIdNot(
                                email,
                                userId
                            )
                            .isPresent()
                    ) {

                        throw new AdminUserApiException(
                            HttpStatus.CONFLICT,
                            "Email is already used by another user"
                        );
                    }


                    String finalFullName =
                        fullName != null
                            ? fullName
                            : user.getFullName();


                    String finalEmail =
                        email != null
                            ? email
                            : user.getEmail();


                    boolean finalActive =
                        isActive != null
                            ? isActive
                            : Boolean.TRUE.equals(
                                user.getActive()
                            );


                    user.updateAdminManagedUser(
                        fullName,
                        email,
                        accessWeb,
                        accessMobile,
                        isActive
                    );


                    UserEntity updated =
                        userRepository
                            .saveAndFlush(
                                user
                            );


                    String keycloakUserId =
                        updated
                            .getKeycloakUserId();


                    if (
                        keycloakUserId != null &&
                        !keycloakUserId.isBlank()
                    ) {

                        keycloakAdminClient
                            .updateUser(
                                keycloakUserId,
                                finalFullName,
                                finalEmail,
                                finalAccessWeb,
                                finalAccessMobile,
                                finalActive
                            );
                    }


                    return new AdminUserUpdateResponse(

                        "User updated successfully",

                        AdminUserResponse
                            .from(
                                updated
                            )
                    );
                }
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
            KeycloakAdminException exception
        ) {

            throw new AdminUserApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to update user"
            );
        }
    }


    private void requireAdmin(
            AuthenticatedFieldSyncUser user
    ) {

        if (!user.isAdmin()) {

            throw new AdminUserApiException(
                HttpStatus.FORBIDDEN,
                "Admin access is required"
            );
        }
    }


    private String normalizeOptional(
            String value
    ) {

        if (
            value == null ||
            value.trim().isEmpty()
        ) {
            return null;
        }

        return value.trim();
    }


    private String normalizeEmail(
            String value
    ) {

        String normalized =
            normalizeOptional(
                value
            );

        if (normalized == null) {
            return null;
        }

        return normalized
            .toLowerCase();
    }
}