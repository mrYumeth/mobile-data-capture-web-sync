package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.TenantEntity;
import com.fieldsync.api.domain.entity.UserEntity;

import com.fieldsync.api.domain.repository.TenantRepository;
import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.keycloak.KeycloakAdminClient;
import com.fieldsync.api.keycloak.KeycloakUserConflictException;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AdminUserProvisioningServiceTests {

    @Test
    void shouldDeleteKeycloakUserWhenDatabaseCreationFails() {

        UserRepository userRepository =
            mock(
                UserRepository.class
            );

        TenantRepository tenantRepository =
            mock(
                TenantRepository.class
            );

        CurrentUserService currentUserService =
            mock(
                CurrentUserService.class
            );

        TenantContextExecutor tenantContextExecutor =
            mock(
                TenantContextExecutor.class
            );

        KeycloakAdminClient keycloakAdminClient =
            mock(
                KeycloakAdminClient.class
            );


        AuthenticatedFieldSyncUser admin =
            new AuthenticatedFieldSyncUser(

                363,
                608,

                "fieldsync-admin-e2e",
                "fieldsync-admin-e2e@example.test",
                "FieldSync Local Admin",

                "admin",

                true,
                true,

                false,

                "web",

                "admin-keycloak-id"
            );


        when(
            currentUserService
                .requireCurrentUser()
        )
            .thenReturn(
                admin
            );


        doAnswer(
            invocation -> {

                Supplier<?> operation =
                    invocation.getArgument(
                        1
                    );

                return operation.get();
            }
        )
            .when(
                tenantContextExecutor
            )
            .execute(
                eq(608),
                any(Supplier.class)
            );


        when(
            userRepository
                .findByUsernameIgnoreCase(
                    "compensation-user"
                )
        )
            .thenReturn(
                Optional.empty()
            );


        when(
            userRepository
                .findByEmailIgnoreCase(
                    "compensation-user@example.test"
                )
        )
            .thenReturn(
                Optional.empty()
            );


        when(
            keycloakAdminClient
                .createUser(
                    "compensation-user",
                    "compensation-user@example.test",
                    "Compensation User",
                    true,
                    true
                )
        )
            .thenReturn(
                "kc-created-user-id"
            );


        TenantEntity tenant =
            mock(
                TenantEntity.class
            );

        UserEntity createdBy =
            mock(
                UserEntity.class
            );


        when(
            tenantRepository
                .findByIdAndActiveTrue(
                    608
                )
        )
            .thenReturn(
                Optional.of(
                    tenant
                )
            );


        when(
            userRepository
                .findByIdAndTenant_Id(
                    363,
                    608
                )
        )
            .thenReturn(
                Optional.of(
                    createdBy
                )
            );


        when(
            userRepository
                .saveAndFlush(
                    any(
                        UserEntity.class
                    )
                )
        )
            .thenThrow(
                new RuntimeException(
                    "Simulated database failure"
                )
            );


        AdminUserProvisioningService service =
            new AdminUserProvisioningService(

                userRepository,
                tenantRepository,
                currentUserService,
                tenantContextExecutor,
                keycloakAdminClient,

                false
            );


        AdminUserApiException exception =
            catchThrowableOfType(
                () ->
                    service.createUser(
                        new AdminUserCreateRequest(
                            "Compensation User",
                            "compensation-user",
                            "compensation-user@example.test",
                            true,
                            true
                        )
                    ),
                AdminUserApiException.class
            );


        assertThat(exception)
            .isNotNull();

        assertThat(
            exception.getStatus()
        )
            .isEqualTo(
                HttpStatus
                    .INTERNAL_SERVER_ERROR
            );

        assertThat(
            exception.getMessage()
        )
            .isEqualTo(
                "Failed to create user"
            );


        verify(
            keycloakAdminClient
        )
            .deleteUser(
                "kc-created-user-id"
            );
    }


    @Test
    void shouldReturnConflictWhenKeycloakUserAlreadyExists() {

        UserRepository userRepository =
            mock(
                UserRepository.class
            );

        TenantRepository tenantRepository =
            mock(
                TenantRepository.class
            );

        CurrentUserService currentUserService =
            mock(
                CurrentUserService.class
            );

        TenantContextExecutor tenantContextExecutor =
            mock(
                TenantContextExecutor.class
            );

        KeycloakAdminClient keycloakAdminClient =
            mock(
                KeycloakAdminClient.class
            );


        AuthenticatedFieldSyncUser admin =
            new AuthenticatedFieldSyncUser(

                363,
                608,

                "fieldsync-admin-e2e",
                "fieldsync-admin-e2e@example.test",
                "FieldSync Local Admin",

                "admin",

                true,
                true,

                false,

                "web",

                "admin-keycloak-id"
            );


        when(
            currentUserService
                .requireCurrentUser()
        )
            .thenReturn(
                admin
            );


        doAnswer(
            invocation -> {

                Supplier<?> operation =
                    invocation.getArgument(
                        1
                    );

                return operation.get();
            }
        )
            .when(
                tenantContextExecutor
            )
            .execute(
                eq(608),
                any(Supplier.class)
            );


        when(
            userRepository
                .findByUsernameIgnoreCase(
                    "duplicate-user"
                )
        )
            .thenReturn(
                Optional.empty()
            );


        when(
            userRepository
                .findByEmailIgnoreCase(
                    "duplicate-user@example.test"
                )
        )
            .thenReturn(
                Optional.empty()
            );


        when(
            keycloakAdminClient
                .createUser(
                    "duplicate-user",
                    "duplicate-user@example.test",
                    "Duplicate User",
                    true,
                    false
                )
        )
            .thenThrow(
                new KeycloakUserConflictException(
                    "A Keycloak user with this username or email already exists"
                )
            );


        AdminUserProvisioningService service =
            new AdminUserProvisioningService(

                userRepository,
                tenantRepository,
                currentUserService,
                tenantContextExecutor,
                keycloakAdminClient,

                false
            );


        AdminUserApiException exception =
            catchThrowableOfType(
                () ->
                    service.createUser(
                        new AdminUserCreateRequest(
                            "Duplicate User",
                            "duplicate-user",
                            "duplicate-user@example.test",
                            true,
                            false
                        )
                    ),
                AdminUserApiException.class
            );


        assertThat(
            exception.getStatus()
        )
            .isEqualTo(
                HttpStatus.CONFLICT
            );

        assertThat(
            exception.getMessage()
        )
            .isEqualTo(
                "A Keycloak user with this username or email already exists"
            );


        verify(
            keycloakAdminClient,
            never()
        )
            .deleteUser(
                any()
            );
    }
}