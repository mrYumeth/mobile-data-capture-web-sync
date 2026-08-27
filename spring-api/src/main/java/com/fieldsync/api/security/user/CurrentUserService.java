package com.fieldsync.api.security.user;

import org.springframework.security.authentication
    .InsufficientAuthenticationException;

import org.springframework.security.core.Authentication;

import org.springframework.security.core.context
    .SecurityContextHolder;

import org.springframework.security.oauth2.server.resource.authentication
    .JwtAuthenticationToken;

import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final AuthenticatedUserResolver
        authenticatedUserResolver;

    public CurrentUserService(
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        this.authenticatedUserResolver =
            authenticatedUserResolver;
    }

    public AuthenticatedFieldSyncUser
    requireCurrentUser() {

        Authentication authentication =
            SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (
            authentication == null ||
            !authentication.isAuthenticated() ||
            !(authentication
                instanceof JwtAuthenticationToken
                    jwtAuthentication)
        ) {
            throw new InsufficientAuthenticationException(
                "Authenticated Keycloak user is required"
            );
        }

        return authenticatedUserResolver.resolve(
            jwtAuthentication.getToken()
        );
    }
}