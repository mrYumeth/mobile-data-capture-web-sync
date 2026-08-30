package com.fieldsync.api.adminuser;

import com.fieldsync.api.domain.entity.UserEntity;

import java.time.LocalDateTime;


public record AdminUserResponse(

    Integer id,
    Integer tenant_id,

    String username,
    String email,
    String full_name,

    String role,

    Boolean access_web,
    Boolean access_mobile,

    Boolean is_active,

    LocalDateTime confirmed_at,

    Boolean password_change_required,

    String keycloak_user_id,

    LocalDateTime created_at

) {

    public static AdminUserResponse from(
            UserEntity user
    ) {

        return new AdminUserResponse(

            user.getId(),
            user.getTenant().getId(),

            user.getUsername(),
            user.getEmail(),
            user.getFullName(),

            user.getRole(),

            user.getAccessWeb(),
            user.getAccessMobile(),

            user.getActive(),

            user.getConfirmedAt(),

            user.getPasswordChangeRequired(),

            user.getKeycloakUserId(),

            user.getCreatedAt()
        );
    }
}