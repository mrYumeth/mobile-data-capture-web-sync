package com.fieldsync.api.adminuser;


public record AdminUserCreateRequest(

    String fullName,
    String username,
    String email,

    Boolean accessWeb,
    Boolean accessMobile

) {
}