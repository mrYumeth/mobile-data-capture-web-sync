package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.TenantEntity;
import com.fieldsync.api.domain.entity.UserEntity;

import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.keycloak.KeycloakAdminClient;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AdminUserUpdateServiceTests {

    @Test
    void shouldUpdateTenantUserAndKeycloak() {

        UserRepository userRepository =
            mock(
                UserRepository.class
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

                "admin-kc-id"
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
                    invocation
                        .getArgument(
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


        TenantEntity tenant =
            mock(
                TenantEntity.class
            );

        when(
            tenant.getId()
        )
            .thenReturn(
                608
            );


        UserEntity user =
            mock(
                UserEntity.class
            );


        when(user.getId())
            .thenReturn(500);

        when(user.getTenant())
            .thenReturn(tenant);

        when(user.getUsername())
            .thenReturn(
                "fieldsync-user-e2e"
            );

        when(user.getEmail())
            .thenReturn(
                "updated-user@example.test"
            );

        when(user.getFullName())
            .thenReturn(
                "Updated FieldSync User"
            );

        when(user.getRole())
            .thenReturn(
                "user"
            );

        when(user.getAccessWeb())
            .thenReturn(
                false
            );

        when(user.getAccessMobile())
            .thenReturn(
                true
            );

        when(user.getActive())
            .thenReturn(
                true
            );

        when(
            user.getPasswordChangeRequired()
        )
            .thenReturn(
                false
            );

        when(
            user.getKeycloakUserId()
        )
            .thenReturn(
                "kc-user-id"
            );

        when(
            user.getCreatedAt()
        )
            .thenReturn(
                LocalDateTime.now()
            );

        when(
            user.getConfirmedAt()
        )
            .thenReturn(
                LocalDateTime.now()
            );


        when(
            userRepository
                .findByIdAndTenant_Id(
                    500,
                    608
                )
        )
            .thenReturn(
                Optional.of(
                    user
                )
            );


        when(
            userRepository
                .findByEmailIgnoreCaseAndIdNot(
                    "updated-user@example.test",
                    500
                )
        )
            .thenReturn(
                Optional.empty()
            );


        when(
            userRepository
                .saveAndFlush(
                    user
                )
        )
            .thenReturn(
                user
            );


        AdminUserUpdateService service =
            new AdminUserUpdateService(

                userRepository,
                currentUserService,
                tenantContextExecutor,
                keycloakAdminClient
            );


        AdminUserUpdateResponse response =
            service.updateUser(

                500,

                new AdminUserUpdateRequest(

                    "Updated FieldSync User",

                    "updated-user@example.test",

                    false,

                    true,

                    true
                )
            );


        assertThat(
            response.message()
        )
            .isEqualTo(
                "User updated successfully"
            );


        verify(
            user
        )
            .updateAdminManagedUser(

                "Updated FieldSync User",

                "updated-user@example.test",

                false,

                true,

                true
            );


        verify(
            keycloakAdminClient
        )
            .updateUser(

                "kc-user-id",

                "Updated FieldSync User",

                "updated-user@example.test",

                false,

                true,

                true
            );
    }


    @Test
    void shouldRejectAdminTarget() {

        UserRepository userRepository =
            mock(
                UserRepository.class
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

                "admin-kc-id"
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
                    invocation
                        .getArgument(
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


        UserEntity protectedAdmin =
            mock(
                UserEntity.class
            );


        when(
            protectedAdmin.getRole()
        )
            .thenReturn(
                "admin"
            );


        when(
            userRepository
                .findByIdAndTenant_Id(
                    999,
                    608
                )
        )
            .thenReturn(
                Optional.of(
                    protectedAdmin
                )
            );


        AdminUserUpdateService service =
            new AdminUserUpdateService(

                userRepository,
                currentUserService,
                tenantContextExecutor,
                keycloakAdminClient
            );


        assertThatThrownBy(
            () ->
                service.updateUser(

                    999,

                    new AdminUserUpdateRequest(

                        "Changed Admin",

                        "changed@example.test",

                        true,

                        true,

                        true
                    )
                )
        )
            .isInstanceOf(
                AdminUserApiException.class
            )
            .hasMessage(
                "Admin account details cannot be edited from user management"
            );
    }
}