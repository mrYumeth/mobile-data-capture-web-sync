package com.fieldsync.api.capturedrecord;

import org.springframework.http.HttpStatus;


public class CapturedRecordApiException
        extends RuntimeException {

    private final HttpStatus status;


    public CapturedRecordApiException(
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