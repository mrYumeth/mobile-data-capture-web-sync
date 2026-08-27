package com.fieldsync.api.category;

import com.fieldsync.api.domain.repository.CategoryRepository;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository
        categoryRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;

    public CategoryService(
            CategoryRepository categoryRepository,
            CurrentUserService currentUserService,
            TenantContextExecutor tenantContextExecutor
    ) {

        this.categoryRepository =
            categoryRepository;

        this.currentUserService =
            currentUserService;

        this.tenantContextExecutor =
            tenantContextExecutor;
    }

    public List<CategoryResponse>
    getActiveCategories() {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        Integer tenantId =
            currentUser.tenantId();

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                categoryRepository
                    .findAllByTenant_IdAndActiveTrueOrderByIdDesc(
                        tenantId
                    )
                    .stream()
                    .map(
                        CategoryResponse::from
                    )
                    .toList()
        );
    }
}