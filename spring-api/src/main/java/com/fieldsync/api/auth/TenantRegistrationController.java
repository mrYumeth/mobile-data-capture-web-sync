package com.fieldsync.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class TenantRegistrationController {

    private final TenantRegistrationService
        tenantRegistrationService;


    public TenantRegistrationController(
            TenantRegistrationService tenantRegistrationService
    ) {

        this.tenantRegistrationService =
            tenantRegistrationService;
    }


    @PostMapping("/register-tenant")
    public ResponseEntity<TenantRegistrationResponse>
    registerTenant(

            @RequestBody
            TenantRegistrationRequest request
    ) {

        return ResponseEntity
            .status(
                HttpStatus.CREATED
            )
            .body(
                tenantRegistrationService
                    .register(
                        request
                    )
            );
    }


    @ExceptionHandler(
        TenantRegistrationApiException.class
    )
    public ResponseEntity<Map<String, String>>
    handleRegistrationException(

            TenantRegistrationApiException exception
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
}