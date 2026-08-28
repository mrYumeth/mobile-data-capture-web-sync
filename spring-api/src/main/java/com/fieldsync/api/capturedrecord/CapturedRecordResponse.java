package com.fieldsync.api.capturedrecord;

import com.fieldsync.api.domain.entity.CapturedRecordEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import java.util.List;


public record CapturedRecordResponse(

    Integer id,
    Integer tenant_id,

    Integer customer_id,
    Integer location_id,
    Integer category_id,

    String customer_name,
    String location_name,
    String category_name,

    String description,

    BigDecimal latitude,
    BigDecimal longitude,

    String image_url,
    String image_path,

    LocalDateTime captured_at,
    LocalDateTime received_at,
    LocalDateTime created_at,
    LocalDateTime updated_at,

    List<CapturedImageResponse> images,

    String full_image_url

) {

    public static CapturedRecordResponse from(
            CapturedRecordEntity record,
            List<CapturedImageResponse> images,
            String fullImageUrl
    ) {

        return new CapturedRecordResponse(

            record.getId(),
            record.getTenant().getId(),

            record.getCustomer() == null
                ? null
                : record.getCustomer().getId(),

            record.getLocation() == null
                ? null
                : record.getLocation().getId(),

            record.getCategory() == null
                ? null
                : record.getCategory().getId(),

            record.getCustomer() == null
                ? null
                : record.getCustomer().getName(),

            record.getLocation() == null
                ? null
                : record.getLocation().getName(),

            record.getCategory() == null
                ? null
                : record.getCategory().getName(),

            record.getDescription(),

            record.getLatitude(),
            record.getLongitude(),

            record.getImageUrl(),
            record.getImagePath(),

            record.getCapturedAt(),
            record.getReceivedAt(),
            record.getCreatedAt(),
            record.getUpdatedAt(),

            images,
            fullImageUrl
        );
    }
}