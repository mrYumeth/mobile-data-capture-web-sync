package com.fieldsync.api.adminuser;


public record AdminUserCreateResponse(

    String message,

    AdminUserResponse user,

    boolean emailSent,

    String keycloakUserId,

    boolean keycloakInviteSent,

    String keycloakTemporaryPassword,

    String setupLink,

    String mobileAppDownloadUrl

) {
}