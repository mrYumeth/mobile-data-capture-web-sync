package com.fieldsync.api.auth;

import org.springframework.http.HttpStatus;


public class TenantRegistrationApiException
        extends RuntimeException {

    private final HttpStatus status;


    public TenantRegistrationApiException(
            HttpStatus status,
            String message
    ) {

        super(message);

        this.status =
            status;
    }


    public HttpStatus getStatus() {
        return status;
    }
}