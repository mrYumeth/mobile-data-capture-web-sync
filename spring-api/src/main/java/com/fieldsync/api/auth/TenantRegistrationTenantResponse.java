package com.fieldsync.api.auth;

import com.fieldsync.api.domain.entity.TenantEntity;

import java.time.LocalDateTime;


public record TenantRegistrationTenantResponse(

    Integer id,

    String name,
    String slug,

    Boolean is_active,

    LocalDateTime created_at

) {

    public static TenantRegistrationTenantResponse from(
            TenantEntity tenant
    ) {

        return new TenantRegistrationTenantResponse(

            tenant.getId(),

            tenant.getName(),
            tenant.getSlug(),

            tenant.getActive(),

            tenant.getCreatedAt()
        );
    }
}