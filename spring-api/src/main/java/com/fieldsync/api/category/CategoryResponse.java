package com.fieldsync.api.category;

import com.fieldsync.api.domain.entity.CategoryEntity;

import java.time.LocalDateTime;

public record CategoryResponse(

    Integer id,
    Integer tenant_id,

    String name,
    String description,

    Boolean is_active,

    LocalDateTime created_at,
    LocalDateTime updated_at

) {

    public static CategoryResponse from(
            CategoryEntity category
    ) {

        return new CategoryResponse(
            category.getId(),
            category.getTenant().getId(),

            category.getName(),
            category.getDescription(),

            category.getActive(),

            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}