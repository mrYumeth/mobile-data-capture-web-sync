package com.fieldsync.api.auth;


public record TenantRegistrationRequest(

    String tenantName,
    String tenantSlug,

    String fullName,
    String username,
    String email

) {
}