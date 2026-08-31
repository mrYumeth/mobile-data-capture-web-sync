package com.fieldsync.api.adminuser;


public record AdminUserAccessRequest(

    Boolean accessWeb,
    Boolean accessMobile,
    Boolean isActive

) {
}