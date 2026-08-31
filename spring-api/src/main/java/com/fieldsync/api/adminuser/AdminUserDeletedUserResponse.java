package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.UserEntity;


public record AdminUserDeletedUserResponse(

    Integer id,
    Integer tenant_id,

    String username,
    String email,
    String full_name,

    String role

) {

    public static AdminUserDeletedUserResponse from(
            UserEntity user
    ) {

        return new AdminUserDeletedUserResponse(

            user.getId(),
            user.getTenant().getId(),

            user.getUsername(),
            user.getEmail(),
            user.getFullName(),

            user.getRole()
        );
    }
}