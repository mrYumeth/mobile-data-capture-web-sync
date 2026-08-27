package com.fieldsync.api.security.user;

public record AuthenticatedFieldSyncUser(

    Integer userId,
    Integer tenantId,

    String username,
    String email,
    String fullName,

    String role,

    boolean accessWeb,
    boolean accessMobile,

    boolean passwordChangeRequired,

    String clientType,

    String keycloakUserId

) {

    public boolean isAdmin() {
        return "admin".equals(role);
    }
}