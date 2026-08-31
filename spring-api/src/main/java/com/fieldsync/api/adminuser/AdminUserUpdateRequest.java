package com.fieldsync.api.adminuser;


public record AdminUserUpdateRequest(

    String fullName,

    String email,

    Boolean accessWeb,

    Boolean accessMobile,

    Boolean isActive

) {
}