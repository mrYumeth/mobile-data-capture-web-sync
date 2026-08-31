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
public class AdminUserDeleteService {

    private final UserRepository
        userRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;

    private final KeycloakAdminClient
        keycloakAdminClient;


    public AdminUserDeleteService(
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


    public AdminUserDeleteResponse deleteUser(
            Integer userId
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
        ) {

            throw new AdminUserApiException(
                HttpStatus.BAD_REQUEST,
                "You cannot delete your own admin account"
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
                            "Admin accounts cannot be deleted from user management"
                        );
                    }


                    AdminUserDeletedUserResponse
                        deletedUser =
                            AdminUserDeletedUserResponse
                                .from(
                                    user
                                );


                    String keycloakUserId =
                        user.getKeycloakUserId();


                    /*
                     * Delete and flush PostgreSQL first.
                     *
                     * If PostgreSQL rejects the deletion,
                     * Keycloak is never touched.
                     */
                    userRepository.delete(
                        user
                    );

                    userRepository.flush();


                    /*
                     * This runs before the surrounding
                     * TenantContextExecutor transaction commits.
                     *
                     * If Keycloak deletion fails, the thrown
                     * exception causes the PostgreSQL transaction
                     * to roll back.
                     */
                    if (
                        keycloakUserId != null &&
                        !keycloakUserId.isBlank()
                    ) {

                        keycloakAdminClient
                            .deleteUser(
                                keycloakUserId
                            );
                    }


                    return new AdminUserDeleteResponse(

                        "User permanently deleted successfully",

                        deletedUser
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
                "Failed to delete user"
            );

        }
        catch (
            RuntimeException exception
        ) {

            throw new AdminUserApiException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Failed to delete user"
            );
        }
    }
}