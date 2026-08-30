package com.fieldsync.api.adminuser;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/admin/users")
public class AdminUserCreateController {

    private final AdminUserProvisioningService
        provisioningService;


    public AdminUserCreateController(
            AdminUserProvisioningService provisioningService
    ) {

        this.provisioningService =
            provisioningService;
    }


    @PostMapping
    public ResponseEntity<AdminUserCreateResponse>
    createUser(
            @RequestBody(required = false)
            AdminUserCreateRequest request
    ) {

        return ResponseEntity
            .status(
                HttpStatus.CREATED
            )
            .body(
                provisioningService
                    .createUser(
                        request
                    )
            );
    }


    @ExceptionHandler(
        AdminUserApiException.class
    )
    public ResponseEntity<Map<String, String>>
    handleAdminUserApiException(
            AdminUserApiException exception
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