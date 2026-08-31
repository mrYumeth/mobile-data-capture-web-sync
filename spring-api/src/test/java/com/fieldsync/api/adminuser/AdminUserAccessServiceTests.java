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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


class AdminUserAccessServiceTests {

    @Test
    void shouldUpdateAccessAndSynchronizeKeycloak() {

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
                "fieldsync-user-updated@example.test"
            );

        when(user.getFullName())
            .thenReturn(
                "FieldSync Updated User"
            );

        when(user.getRole())
            .thenReturn(
                "user"
            );

        when(user.getAccessWeb())
            .thenReturn(
                true
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
                .saveAndFlush(
                    user
                )
        )
            .thenReturn(
                user
            );


        AdminUserAccessService service =
            new AdminUserAccessService(

                userRepository,
                currentUserService,
                tenantContextExecutor,
                keycloakAdminClient
            );


        AdminUserAccessResponse response =
            service.updateAccess(

                500,

                new AdminUserAccessRequest(
                    false,
                    false,
                    true
                )
            );


        assertThat(
            response.message()
        )
            .isEqualTo(
                "User access updated successfully"
            );


        verify(
            user
        )
            .updateAdminManagedUser(
                null,
                null,
                false,
                false,
                true
            );


        verify(
            keycloakAdminClient
        )
            .updateUser(
                "kc-user-id",
                "FieldSync Updated User",
                "fieldsync-user-updated@example.test",
                false,
                false,
                true
            );
    }
}