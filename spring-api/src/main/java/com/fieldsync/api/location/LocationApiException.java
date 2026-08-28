package com.fieldsync.api.location;

import org.springframework.http.HttpStatus;


public class LocationApiException
        extends RuntimeException {

    private final HttpStatus status;


    public LocationApiException(
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