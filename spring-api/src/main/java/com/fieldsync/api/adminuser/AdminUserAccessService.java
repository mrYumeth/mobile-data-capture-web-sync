package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.UserEntity;
import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.keycloak.KeycloakAdminClient;
import com.fieldsync.api.keycloak.KeycloakAdminException;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;


@Service
public class AdminUserAccessService {

    private final UserRepository
        userRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;

    private final KeycloakAdminClient
        keycloakAdminClient;


    public AdminUserAccessService(
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


    public AdminUserAccessResponse updateAccess(
            Integer userId,
            AdminUserAccessRequest request
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
                            "Admin account access cannot be edited from user management"
                        );
                    }


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


                    boolean finalActive =
                        isActive != null
                            ? isActive
                            : Boolean.TRUE.equals(
                                user.getActive()
                            );


                    user.updateAdminManagedUser(
                        null,
                        null,
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
                                updated.getFullName(),
                                updated.getEmail(),
                                finalAccessWeb,
                                finalAccessMobile,
                                finalActive
                            );
                    }


                    return new AdminUserAccessResponse(
                        "User access updated successfully",
                        AdminUserResponse.from(
                            updated
                        )
                    );
                }
            );

        }
        catch (
            AdminUserApiException exception
        ) {

            throw exception;

        }
        catch (
            KeycloakAdminException exception
        ) {

            throw new AdminUserApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to update user access"
            );

        }
        catch (
            RuntimeException exception
        ) {

            throw new AdminUserApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to update user access"
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
}