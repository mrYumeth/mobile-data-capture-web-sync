package com.fieldsync.api.auth;


public record TenantRegistrationResponse(

    String message,

    TenantRegistrationTenantResponse tenant,

    TenantRegistrationUserResponse user,

    String keycloakUserId,

    String keycloakTemporaryPassword,

    boolean keycloakInviteSent

) {
}