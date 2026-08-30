package com.fieldsync.api.auth;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;


public record AuthUserResponse(

    Integer id,
    Integer tenantId,

    String username,
    String email,
    String fullName,

    String role,

    boolean accessWeb,
    boolean accessMobile,

    boolean passwordChangeRequired,

    String clientType

) {

    public static AuthUserResponse from(
            AuthenticatedFieldSyncUser user
    ) {

        return new AuthUserResponse(

            user.userId(),
            user.tenantId(),

            user.username(),
            user.email(),
            user.fullName(),

            user.role(),

            user.accessWeb(),
            user.accessMobile(),

            user.passwordChangeRequired(),

            user.clientType()
        );
    }
}