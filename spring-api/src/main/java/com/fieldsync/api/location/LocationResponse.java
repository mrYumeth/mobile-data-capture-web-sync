package com.fieldsync.api.location;

import com.fieldsync.api.domain.entity.LocationEntity;

import java.time.LocalDateTime;


public record LocationResponse(

    Integer id,
    Integer tenant_id,

    String name,
    String address,

    Boolean is_active,

    LocalDateTime created_at,
    LocalDateTime updated_at

) {

    public static LocationResponse from(
            LocationEntity location
    ) {

        return new LocationResponse(
            location.getId(),
            location.getTenant().getId(),

            location.getName(),
            location.getAddress(),

            location.getActive(),

            location.getCreatedAt(),
            location.getUpdatedAt()
        );
    }
}