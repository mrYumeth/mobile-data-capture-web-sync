package com.fieldsync.api.capturedrecord;

import com.fieldsync.api.storage.ImageStorageException;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.support
    .ServletUriComponentsBuilder;

import java.math.BigDecimal;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/captured-records")
public class CapturedRecordController {

    private final CapturedRecordService
        capturedRecordService;


    public CapturedRecordController(
            CapturedRecordService capturedRecordService
    ) {

        this.capturedRecordService =
            capturedRecordService;
    }


    // =====================================================
    // Read
    // =====================================================

    @GetMapping
    public List<CapturedRecordResponse>
    getCapturedRecords() {

        return capturedRecordService
            .getCapturedRecords(
                getBaseUrl()
            );
    }


    @GetMapping("/{id}")
    public CapturedRecordResponse
    getCapturedRecord(
            @PathVariable String id
    ) {

        return capturedRecordService
            .getCapturedRecord(
                parseRecordId(id),
                getBaseUrl()
            );
    }


    // =====================================================
    // Create
    // =====================================================

    @PostMapping(
        consumes =
            MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<CapturedRecordCreateResponse>
    createCapturedRecord(

            @RequestParam(
                value = "customer_id",
                required = false
            )
            String customerId,

            @RequestParam(
                value = "location_id",
                required = false
            )
            String locationId,

            @RequestParam(
                value = "category_id",
                required = false
            )
            String categoryId,

            @RequestParam(
                value = "description",
                required = false
            )
            String description,

            @RequestParam(
                value = "latitude",
                required = false
            )
            String latitude,

            @RequestParam(
                value = "longitude",
                required = false
            )
            String longitude,

            @RequestParam(
                value = "captured_at",
                required = false
            )
            String capturedAt,

            @RequestParam(
                value = "images",
                required = false
            )
            List<MultipartFile> images,

            @RequestParam(
                value = "image",
                required = false
            )
            MultipartFile legacyImage
    ) {

        CapturedRecordCreateRequest request =
            new CapturedRecordCreateRequest(
                customerId,
                locationId,
                categoryId,
                description,
                latitude,
                longitude,
                capturedAt
            );


        CapturedRecordCreateResponse response =
            capturedRecordService
                .createCapturedRecord(
                    request,
                    images,
                    legacyImage,
                    getBaseUrl()
                );


        return ResponseEntity
            .status(
                HttpStatus.CREATED
            )
            .body(
                response
            );
    }


    // =====================================================
    // Error handling
    // =====================================================

    @ExceptionHandler(
        CapturedRecordApiException.class
    )
    public ResponseEntity<Map<String, String>>
    handleCapturedRecordApiException(
            CapturedRecordApiException exception
    ) {

        return ResponseEntity
            .status(
                exception.getStatus()
            )
            .body(
                Map.of(
                    "message",
                    exception.getMessage()
                )
            );
    }


    @ExceptionHandler(
        ImageStorageException.class
    )
    public ResponseEntity<Map<String, String>>
    handleImageStorageException(
            ImageStorageException exception
    ) {

        return ResponseEntity
            .status(
                HttpStatus.INTERNAL_SERVER_ERROR
            )
            .body(
                Map.of(
                    "message",
                    "Failed to create captured record"
                )
            );
    }


    // =====================================================
    // Helpers
    // =====================================================

    private Integer parseRecordId(
            String id
    ) {

        try {

            BigDecimal numericId =
                new BigDecimal(
                    id.trim()
                );

            int value =
                numericId
                    .intValueExact();


            if (value <= 0) {

                throw new ArithmeticException();
            }


            return value;
        }
        catch (
            NumberFormatException |
            ArithmeticException exception
        ) {

            throw new CapturedRecordApiException(
                HttpStatus.BAD_REQUEST,
                "Invalid captured record ID"
            );
        }
    }


    private String getBaseUrl() {

        return ServletUriComponentsBuilder
            .fromCurrentContextPath()
            .build()
            .toUriString();
    }
}