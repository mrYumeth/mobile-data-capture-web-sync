package com.fieldsync.api.adminuser;


public record AdminUserDeleteResponse(

    String message,
    AdminUserDeletedUserResponse user

) {
}