package com.fieldsync.api.adminuser;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/api/admin/users")
public class AdminUserAccessController {

    private final AdminUserAccessService
        adminUserAccessService;


    public AdminUserAccessController(
            AdminUserAccessService adminUserAccessService
    ) {

        this.adminUserAccessService =
            adminUserAccessService;
    }


    @PatchMapping("/{id}/access")
    public AdminUserAccessResponse updateAccess(

            @PathVariable("id")
            Integer userId,

            @RequestBody(required = false)
            AdminUserAccessRequest request
    ) {

        return adminUserAccessService
            .updateAccess(
                userId,
                request
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