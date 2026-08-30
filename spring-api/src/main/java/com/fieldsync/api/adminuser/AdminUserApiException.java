package com.fieldsync.api.adminuser;

import org.springframework.http.HttpStatus;


public class AdminUserApiException
        extends RuntimeException {

    private final HttpStatus status;


    public AdminUserApiException(
            HttpStatus status,
            String message
    ) {

        super(message);

        this.status = status;
    }


    public HttpStatus getStatus() {
        return status;
    }
}