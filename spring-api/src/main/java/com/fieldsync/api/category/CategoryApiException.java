package com.fieldsync.api.category;

import org.springframework.http.HttpStatus;

public class CategoryApiException
        extends RuntimeException {

    private final HttpStatus status;

    public CategoryApiException(
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