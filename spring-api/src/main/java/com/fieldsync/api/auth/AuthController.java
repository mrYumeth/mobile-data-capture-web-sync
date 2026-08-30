package com.fieldsync.api.auth;

import com.fieldsync.api.security.user.AuthenticatedFieldSyncUser;
import com.fieldsync.api.security.user.CurrentUserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CurrentUserService
        currentUserService;


    public AuthController(
            CurrentUserService currentUserService
    ) {

        this.currentUserService =
            currentUserService;
    }


    @GetMapping("/me")
    public AuthMeResponse getCurrentUser() {

        AuthenticatedFieldSyncUser user =
            currentUserService
                .requireCurrentUser();


        return new AuthMeResponse(
            AuthUserResponse.from(
                user
            )
        );
    }
}