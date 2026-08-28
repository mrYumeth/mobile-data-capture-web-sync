package com.fieldsync.api.capturedrecord;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

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