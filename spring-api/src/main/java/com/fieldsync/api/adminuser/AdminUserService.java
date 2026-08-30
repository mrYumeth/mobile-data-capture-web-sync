package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.repository.UserRepository;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdminUserService {

    private final UserRepository
        userRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;


    public AdminUserService(
            UserRepository userRepository,
            CurrentUserService currentUserService,
            TenantContextExecutor tenantContextExecutor
    ) {

        this.userRepository =
            userRepository;

        this.currentUserService =
            currentUserService;

        this.tenantContextExecutor =
            tenantContextExecutor;
    }


    public List<AdminUserResponse>
    getUsers() {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();


        requireAdmin(
            currentUser
        );


        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () ->
                userRepository
                    .findAllByTenant_IdOrderByCreatedAtDesc(
                        tenantId
                    )
                    .stream()
                    .map(
                        AdminUserResponse::from
                    )
                    .toList()
        );
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