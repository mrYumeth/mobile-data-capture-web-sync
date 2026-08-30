package com.fieldsync.api.adminuser;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService
        adminUserService;


    public AdminUserController(
            AdminUserService adminUserService
    ) {

        this.adminUserService =
            adminUserService;
    }


    @GetMapping
    public List<AdminUserResponse>
    getUsers() {

        return adminUserService
            .getUsers();
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