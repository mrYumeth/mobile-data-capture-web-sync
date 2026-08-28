package com.fieldsync.api.capturedrecord;

import com.fieldsync.api.domain.entity.CapturedImageEntity;

import java.time.LocalDateTime;


public record CapturedImageResponse(

    Integer id,
    Integer captured_record_id,
    Integer tenant_id,

    String image_url,
    String storage_path,

    LocalDateTime created_at,

    String full_image_url

) {

    public static CapturedImageResponse from(
            CapturedImageEntity image,
            String fullImageUrl
    ) {

        return new CapturedImageResponse(

            image.getId(),

            image.getCapturedRecord() == null
                ? null
                : image.getCapturedRecord().getId(),

            image.getTenant().getId(),

            image.getImageUrl(),
            image.getStoragePath(),

            image.getCreatedAt(),

            fullImageUrl
        );
    }
}