package com.fieldsync.api.category;

public record CategoryDeleteResponse(

    String message,
    CategoryResponse deletedCategory

) {
}