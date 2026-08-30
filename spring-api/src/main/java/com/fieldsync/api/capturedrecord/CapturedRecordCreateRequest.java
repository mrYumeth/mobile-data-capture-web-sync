package com.fieldsync.api.capturedrecord;


public record CapturedRecordCreateRequest(

    String customerId,
    String locationId,
    String categoryId,

    String description,

    String latitude,
    String longitude,

    String capturedAt

) {
}