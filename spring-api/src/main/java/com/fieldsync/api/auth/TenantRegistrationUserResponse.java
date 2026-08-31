package com.fieldsync.api.auth;

import com.fieldsync.api.domain.entity.UserEntity;


public record TenantRegistrationUserResponse(

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

    public static TenantRegistrationUserResponse from(
            UserEntity user
    ) {

        return new TenantRegistrationUserResponse(

            user.getId(),
            user.getTenant().getId(),

            user.getUsername(),
            user.getEmail(),
            user.getFullName(),

            user.getRole(),

            Boolean.TRUE.equals(
                user.getAccessWeb()
            ),

            Boolean.TRUE.equals(
                user.getAccessMobile()
            ),

            Boolean.TRUE.equals(
                user.getPasswordChangeRequired()
            ),

            "web"
        );
    }
}