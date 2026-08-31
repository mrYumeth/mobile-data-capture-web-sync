package com.fieldsync.api.adminuser;


public record AdminUserAccessResponse(

    String message,
    AdminUserResponse user

) {
}