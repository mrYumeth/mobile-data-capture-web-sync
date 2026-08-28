package com.fieldsync.api.category;

import com.fieldsync.api.domain.entity.CategoryEntity;
import com.fieldsync.api.domain.entity.TenantEntity;

import com.fieldsync.api.domain.repository.CategoryRepository;
import com.fieldsync.api.domain.repository.TenantRepository;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CategoryService {

    private final CategoryRepository
        categoryRepository;

    private final TenantRepository
        tenantRepository;

    private final CurrentUserService
        currentUserService;

    private final TenantContextExecutor
        tenantContextExecutor;


    public CategoryService(
            CategoryRepository categoryRepository,
            TenantRepository tenantRepository,
            CurrentUserService currentUserService,
            TenantContextExecutor tenantContextExecutor
    ) {

        this.categoryRepository =
            categoryRepository;

        this.tenantRepository =
            tenantRepository;

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
                    .map(CategoryResponse::from)
                    .toList()
        );
    }


    public CategoryResponse createCategory(
            CategoryRequest request
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        requireWebAccess(currentUser);

        String name =
            requireName(request);

        String description =
            normalizeDescription(
                request.description()
            );

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                TenantEntity tenant =
                    tenantRepository
                        .findByIdAndActiveTrue(
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new CategoryApiException(
                                    HttpStatus.FORBIDDEN,
                                    "Tenant is not active"
                                )
                        );

                CategoryEntity category =
                    CategoryEntity.create(
                        tenant,
                        name,
                        description
                    );

                return CategoryResponse.from(
                    categoryRepository
                        .saveAndFlush(category)
                );
            }
        );
    }


    public CategoryResponse updateCategory(
            Integer categoryId,
            CategoryRequest request
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        requireWebAccess(currentUser);

        String name =
            requireName(request);

        String description =
            normalizeDescription(
                request.description()
            );

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                CategoryEntity category =
                    categoryRepository
                        .findByIdAndTenant_Id(
                            categoryId,
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new CategoryApiException(
                                    HttpStatus.NOT_FOUND,
                                    "Category not found"
                                )
                        );

                category.update(
                    name,
                    description
                );

                return CategoryResponse.from(
                    categoryRepository
                        .saveAndFlush(category)
                );
            }
        );
    }


    public CategoryDeleteResponse deleteCategory(
            Integer categoryId
    ) {

        AuthenticatedFieldSyncUser currentUser =
            currentUserService
                .requireCurrentUser();

        requireAdmin(currentUser);

        Integer tenantId =
            currentUser.tenantId();


        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                CategoryEntity category =
                    categoryRepository
                        .findByIdAndTenant_Id(
                            categoryId,
                            tenantId
                        )
                        .orElseThrow(
                            () ->
                                new CategoryApiException(
                                    HttpStatus.NOT_FOUND,
                                    "Category not found"
                                )
                        );

                CategoryResponse deleted =
                    CategoryResponse.from(
                        category
                    );

                categoryRepository.delete(
                    category
                );

                categoryRepository.flush();

                return new CategoryDeleteResponse(
                    "Category deleted successfully",
                    deleted
                );
            }
        );
    }


    private String requireName(
            CategoryRequest request
    ) {

        if (
            request == null ||
            request.name() == null ||
            request.name().trim().isEmpty()
        ) {

            throw new CategoryApiException(
                HttpStatus.BAD_REQUEST,
                "Category name is required"
            );
        }

        return request.name().trim();
    }


    private String normalizeDescription(
            String description
    ) {

        if (
            description == null ||
            description.isEmpty()
        ) {
            return null;
        }

        return description;
    }


    private void requireWebAccess(
            AuthenticatedFieldSyncUser user
    ) {

        if (
            !user.isAdmin() &&
            !user.accessWeb()
        ) {

            throw new CategoryApiException(
                HttpStatus.FORBIDDEN,
                "Web application access is required"
            );
        }
    }


    private void requireAdmin(
            AuthenticatedFieldSyncUser user
    ) {

        if (!user.isAdmin()) {

            throw new CategoryApiException(
                HttpStatus.FORBIDDEN,
                "Admin access is required"
            );
        }
    }
}