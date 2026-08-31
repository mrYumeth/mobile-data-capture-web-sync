package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.TenantEntity;
import com.fieldsync.api.domain.entity.UserEntity;

import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.keycloak.KeycloakAdminClient;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AdminUserDeleteServiceTests {

    @Test
    void shouldDeleteTenantUserAndKeycloakUser() {

        UserRepository userRepository =
            mock(UserRepository.class);

        CurrentUserService currentUserService =
            mock(CurrentUserService.class);

        TenantContextExecutor tenantContextExecutor =
            mock(TenantContextExecutor.class);

        KeycloakAdminClient keycloakAdminClient =
            mock(KeycloakAdminClient.class);


        AuthenticatedFieldSyncUser admin =
            new AuthenticatedFieldSyncUser(

                363,
                608,

                "fieldsync-admin-e2e",
                "fieldsync-admin-e2e@example.test",
                "FieldSync Admin",

                "admin",

                true,
                true,

                false,

                "web",

                "admin-kc-id"
            );


        when(
            currentUserService
                .requireCurrentUser()
        )
            .thenReturn(admin);


        doAnswer(
            invocation -> {

                Supplier<?> operation =
                    invocation.getArgument(1);

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


        TenantEntity tenant =
            mock(TenantEntity.class);

        when(
            tenant.getId()
        )
            .thenReturn(608);


        UserEntity user =
            mock(UserEntity.class);

        when(user.getId())
            .thenReturn(500);

        when(user.getTenant())
            .thenReturn(tenant);

        when(user.getUsername())
            .thenReturn("delete-user");

        when(user.getEmail())
            .thenReturn(
                "delete-user@example.test"
            );

        when(user.getFullName())
            .thenReturn(
                "Delete User"
            );

        when(user.getRole())
            .thenReturn("user");

        when(
            user.getKeycloakUserId()
        )
            .thenReturn(
                "kc-delete-user-id"
            );


        when(
            userRepository
                .findByIdAndTenant_Id(
                    500,
                    608
                )
        )
            .thenReturn(
                Optional.of(user)
            );


        AdminUserDeleteService service =
            new AdminUserDeleteService(

                userRepository,
                currentUserService,
                tenantContextExecutor,
                keycloakAdminClient
            );


        AdminUserDeleteResponse response =
            service.deleteUser(500);


        assertThat(
            response.message()
        )
            .isEqualTo(
                "User permanently deleted successfully"
            );


        assertThat(
            response.user().username()
        )
            .isEqualTo(
                "delete-user"
            );


        verify(
            userRepository
        )
            .delete(user);

        verify(
            userRepository
        )
            .flush();

        verify(
            keycloakAdminClient
        )
            .deleteUser(
                "kc-delete-user-id"
            );
    }
}