package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.TenantEntity;
import com.fieldsync.api.domain.entity.UserEntity;

import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatchers;


class AdminUserServiceTests {

    @Test
    void shouldReturnOnlyAuthenticatedAdminTenantUsers() {

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


        AuthenticatedFieldSyncUser currentUser =
            new AuthenticatedFieldSyncUser(

                1,
                7,

                "admin",
                "admin@example.test",
                "Admin User",

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
                currentUser
            );


        TenantEntity tenant =
            mock(
                TenantEntity.class
            );

        when(
            tenant.getId()
        )
            .thenReturn(
                7
            );


        UserEntity user =
            mock(
                UserEntity.class
            );

        when(user.getId())
            .thenReturn(25);

        when(user.getTenant())
            .thenReturn(tenant);

        when(user.getUsername())
            .thenReturn("field-user");

        when(user.getEmail())
            .thenReturn(
                "field@example.test"
            );

        when(user.getFullName())
            .thenReturn(
                "Field User"
            );

        when(user.getRole())
            .thenReturn("user");

        when(user.getAccessWeb())
            .thenReturn(true);

        when(user.getAccessMobile())
            .thenReturn(true);

        when(user.getActive())
            .thenReturn(true);

        when(user.getPasswordChangeRequired())
            .thenReturn(false);

        when(user.getKeycloakUserId())
            .thenReturn(
                "field-keycloak-id"
            );

        when(user.getCreatedAt())
            .thenReturn(
                LocalDateTime.now()
            );


        when(
            userRepository
                .findAllByTenant_IdOrderByCreatedAtDesc(
                    7
                )
        )
            .thenReturn(
                List.of(
                    user
                )
            );


        when(
            tenantContextExecutor.execute(
                eq(7),
                ArgumentMatchers
                    .<Supplier<List<AdminUserResponse>>>any()
            )
        )
            .thenAnswer(
                invocation -> {

                    Supplier<List<AdminUserResponse>>
                        operation =
                            invocation.getArgument(
                                1
                            );

                    return operation.get();
                }
            );


        AdminUserService service =
            new AdminUserService(
                userRepository,
                currentUserService,
                tenantContextExecutor
            );


        List<AdminUserResponse> users =
            service.getUsers();


        assertThat(users)
            .hasSize(1);

        assertThat(
            users.getFirst().tenant_id()
        )
            .isEqualTo(7);


        verify(
            userRepository
        )
            .findAllByTenant_IdOrderByCreatedAtDesc(
                7
            );
    }


    @Test
    void shouldRejectNonAdminUser() {

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


        AuthenticatedFieldSyncUser currentUser =
            new AuthenticatedFieldSyncUser(

                22,
                7,

                "normal-user",
                "user@example.test",
                "Normal User",

                "user",

                true,
                false,

                false,

                "web",

                "user-keycloak-id"
            );


        when(
            currentUserService
                .requireCurrentUser()
        )
            .thenReturn(
                currentUser
            );


        AdminUserService service =
            new AdminUserService(
                userRepository,
                currentUserService,
                tenantContextExecutor
            );


        assertThatThrownBy(
            service::getUsers
        )
            .isInstanceOf(
                AdminUserApiException.class
            )
            .hasMessage(
                "Admin access is required"
            );
    }
}