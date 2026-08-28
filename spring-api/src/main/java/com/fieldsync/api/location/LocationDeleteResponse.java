package com.fieldsync.api.location;

public record LocationDeleteResponse(

    String message,
    LocationResponse deletedLocation

) {
}