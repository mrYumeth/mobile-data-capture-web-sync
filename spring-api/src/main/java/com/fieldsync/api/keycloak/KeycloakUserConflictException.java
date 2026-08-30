package com.fieldsync.api.keycloak;


public class KeycloakUserConflictException
        extends KeycloakAdminException {

    public KeycloakUserConflictException(
            String message
    ) {
        super(message);
    }
}