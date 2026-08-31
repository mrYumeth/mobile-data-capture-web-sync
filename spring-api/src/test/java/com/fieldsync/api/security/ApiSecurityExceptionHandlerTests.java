package com.fieldsync.api.security;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


class ApiSecurityExceptionHandlerTests {


    @Test
    void shouldReturnForbiddenMessageForAccessDenied() {

        ApiSecurityExceptionHandler handler =
            new ApiSecurityExceptionHandler();


        ResponseEntity<Map<String, String>> response =
            handler.handleAccessDenied(
                new AccessDeniedException(
                    "Your account is not allowed to access the web application"
                )
            );


        assertThat(
            response.getStatusCode()
        )
            .isEqualTo(
                HttpStatus.FORBIDDEN
            );


        assertThat(
            response.getBody()
        )
            .containsEntry(
                "message",
                "Your account is not allowed to access the web application"
            );
    }


    @Test
    void shouldReturnUnauthorizedMessageForMissingAuthentication() {

        ApiSecurityExceptionHandler handler =
            new ApiSecurityExceptionHandler();


        ResponseEntity<Map<String, String>> response =
            handler.handleInsufficientAuthentication(
                new InsufficientAuthenticationException(
                    "Authenticated Keycloak user is required"
                )
            );


        assertThat(
            response.getStatusCode()
        )
            .isEqualTo(
                HttpStatus.UNAUTHORIZED
            );


        assertThat(
            response.getBody()
        )
            .containsEntry(
                "message",
                "Authenticated Keycloak user is required"
            );
    }
}