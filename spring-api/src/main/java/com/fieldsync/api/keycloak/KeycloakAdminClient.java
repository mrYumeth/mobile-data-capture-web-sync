package com.fieldsync.api.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.stereotype.Component;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


@Component
public class KeycloakAdminClient {

    private final RestClient restClient;

    private final String realm;
    private final String clientId;
    private final String clientSecret;


    public KeycloakAdminClient(

            @Qualifier(
                "keycloakAdminRestClientBuilder"
            )
            RestClient.Builder restClientBuilder,

            @Value(
                "${fieldsync.keycloak.admin.base-url}"
            )
            String baseUrl,

            @Value(
                "${fieldsync.keycloak.admin.realm}"
            )
            String realm,

            @Value(
                "${fieldsync.keycloak.admin.client-id}"
            )
            String clientId,

            @Value(
                "${fieldsync.keycloak.admin.client-secret}"
            )
            String clientSecret
    ) {

        this.restClient =
            restClientBuilder
                .baseUrl(
                    removeTrailingSlash(
                        baseUrl
                    )
                )
                .build();

        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }


    public boolean verifyUserManagementAccess() {

        String accessToken =
            getAdminAccessToken();

        try {

            restClient
                .get()
                .uri(
                    "/admin/realms/{realm}/users?max=1",
                    realm
                )
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + accessToken
                )
                .retrieve()
                .toBodilessEntity();

            return true;

        }
        catch (
            RestClientResponseException exception
        ) {

            throw new KeycloakAdminException(
                "Keycloak Admin API user-management access failed with HTTP "
                    + exception
                        .getStatusCode()
                        .value(),
                exception
            );
        }
    }

    public String createUser(
        String username,
        String email,
        String fullName,
        boolean accessWeb,
        boolean accessMobile
) {

    String accessToken =
        getAdminAccessToken();

    NameParts nameParts =
        splitFullName(
            fullName
        );


    Map<String, Object> attributes =
        new LinkedHashMap<>();

    attributes.put(
        "fieldsync_access_web",
        List.of(
            Boolean.toString(
                accessWeb
            )
        )
    );

    attributes.put(
        "fieldsync_access_mobile",
        List.of(
            Boolean.toString(
                accessMobile
            )
        )
    );


    Map<String, Object> body =
        new LinkedHashMap<>();

    body.put(
        "username",
        username
    );

    body.put(
        "email",
        email
    );

    body.put(
        "firstName",
        nameParts.firstName()
    );

    body.put(
        "lastName",
        nameParts.lastName()
    );

    body.put(
        "enabled",
        true
    );

    body.put(
        "emailVerified",
        true
    );

    body.put(
        "requiredActions",
        List.of(
            "UPDATE_PASSWORD"
        )
    );

    body.put(
        "attributes",
        attributes
    );


    try {

        ResponseEntity<Void> response =
            restClient
                .post()
                .uri(
                    "/admin/realms/{realm}/users",
                    realm
                )
                .header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + accessToken
                )
                .contentType(
                    MediaType.APPLICATION_JSON
                )
                .body(body)
                .retrieve()
                .toBodilessEntity();


        String location =
            response
                .getHeaders()
                .getFirst(
                    HttpHeaders.LOCATION
                );


        String keycloakUserId =
            getUserIdFromLocation(
                location
            );


        if (
            keycloakUserId == null ||
            keycloakUserId.isBlank()
        ) {

            throw new KeycloakAdminException(
                "Keycloak user was created, but user ID was not returned"
            );
        }


        return keycloakUserId;

    }
    catch (
        RestClientResponseException exception
    ) {

        if (
            exception
                .getStatusCode()
                .value()
                ==
                HttpStatus.CONFLICT.value()
        ) {

            throw new KeycloakUserConflictException(
                "A Keycloak user with this username or email already exists"
            );
        }


        throw new KeycloakAdminException(
            "Failed to create Keycloak user; HTTP "
                + exception
                    .getStatusCode()
                    .value(),
            exception
        );
    }
}


public void setTemporaryPassword(
        String keycloakUserId,
        String temporaryPassword
) {

    String accessToken =
        getAdminAccessToken();


    Map<String, Object> body =
        new LinkedHashMap<>();

    body.put(
        "type",
        "password"
    );

    body.put(
        "value",
        temporaryPassword
    );

    body.put(
        "temporary",
        true
    );


    try {

        restClient
            .put()
            .uri(
                "/admin/realms/{realm}/users/{userId}/reset-password",
                realm,
                keycloakUserId
            )
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + accessToken
            )
            .contentType(
                MediaType.APPLICATION_JSON
            )
            .body(body)
            .retrieve()
            .toBodilessEntity();

    }
    catch (
        RestClientResponseException exception
    ) {

        throw new KeycloakAdminException(
            "Failed to set Keycloak temporary password; HTTP "
                + exception
                    .getStatusCode()
                    .value(),
            exception
        );
    }
}

public void updateUser(
        String keycloakUserId,
        String fullName,
        String email,
        boolean accessWeb,
        boolean accessMobile,
        boolean active
) {

    if (
        keycloakUserId == null ||
        keycloakUserId.isBlank()
    ) {
        return;
    }


    String accessToken =
        getAdminAccessToken();


    NameParts nameParts =
        splitFullName(
            fullName
        );


    Map<String, Object> attributes =
        new LinkedHashMap<>();

    attributes.put(
        "fieldsync_access_web",
        List.of(
            Boolean.toString(
                accessWeb
            )
        )
    );

    attributes.put(
        "fieldsync_access_mobile",
        List.of(
            Boolean.toString(
                accessMobile
            )
        )
    );


    Map<String, Object> body =
        new LinkedHashMap<>();

    body.put(
        "firstName",
        nameParts.firstName()
    );

    body.put(
        "lastName",
        nameParts.lastName()
    );

    body.put(
        "email",
        email
    );

    body.put(
        "emailVerified",
        true
    );

    body.put(
        "enabled",
        active
    );

    body.put(
        "attributes",
        attributes
    );


    try {

        restClient
            .put()
            .uri(
                "/admin/realms/{realm}/users/{userId}",
                realm,
                keycloakUserId
            )
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + accessToken
            )
            .contentType(
                MediaType.APPLICATION_JSON
            )
            .body(body)
            .retrieve()
            .toBodilessEntity();

    }
    catch (
        RestClientResponseException exception
    ) {

        if (
            exception
                .getStatusCode()
                .value()
                ==
                HttpStatus.CONFLICT.value()
        ) {

            throw new KeycloakUserConflictException(
                "A Keycloak user with this username or email already exists"
            );
        }


        throw new KeycloakAdminException(
            "Failed to update Keycloak user; HTTP "
                + exception
                    .getStatusCode()
                    .value(),
            exception
        );
    }
}


public void deleteUser(
        String keycloakUserId
) {

    if (
        keycloakUserId == null ||
        keycloakUserId.isBlank()
    ) {
        return;
    }


    String accessToken =
        getAdminAccessToken();


    try {

        restClient
            .delete()
            .uri(
                "/admin/realms/{realm}/users/{userId}",
                realm,
                keycloakUserId
            )
            .header(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + accessToken
            )
            .retrieve()
            .toBodilessEntity();

    }
    catch (
        RestClientResponseException exception
    ) {

        if (
            exception
                .getStatusCode()
                .value()
                ==
                HttpStatus.NOT_FOUND.value()
        ) {
            return;
        }


        throw new KeycloakAdminException(
            "Failed to delete Keycloak user; HTTP "
                + exception
                    .getStatusCode()
                    .value(),
            exception
        );
    }
}


    private String getAdminAccessToken() {

        if (
            clientSecret == null ||
            clientSecret.isBlank()
        ) {

            throw new KeycloakAdminException(
                "Keycloak Admin API client secret is not configured"
            );
        }


        MultiValueMap<String, String> form =
            new LinkedMultiValueMap<>();

        form.add(
            "grant_type",
            "client_credentials"
        );

        form.add(
            "client_id",
            clientId
        );

        form.add(
            "client_secret",
            clientSecret
        );


        try {

            TokenResponse response =
                restClient
                    .post()
                    .uri(
                        "/realms/{realm}/protocol/openid-connect/token",
                        realm
                    )
                    .contentType(
                        MediaType
                            .APPLICATION_FORM_URLENCODED
                    )
                    .body(form)
                    .retrieve()
                    .body(
                        TokenResponse.class
                    );


            if (
                response == null ||
                response.accessToken() == null ||
                response.accessToken().isBlank()
            ) {

                throw new KeycloakAdminException(
                    "Keycloak did not return an admin access token"
                );
            }


            return response.accessToken();

        }
        catch (
            RestClientResponseException exception
        ) {

            throw new KeycloakAdminException(
                "Unable to obtain Keycloak Admin API access token; HTTP "
                    + exception
                        .getStatusCode()
                        .value(),
                exception
            );
        }
    }


    private static String removeTrailingSlash(
            String value
    ) {

        if (value == null) {
            return "";
        }

        String result = value.trim();

        while (
            result.endsWith("/")
        ) {

            result =
                result.substring(
                    0,
                    result.length() - 1
                );
        }

        return result;
    }

    private static NameParts splitFullName(
        String fullName
) {

    String normalized =
        fullName.trim();


    String[] parts =
        normalized.split(
            "\\s+",
            2
        );


    if (parts.length == 1) {

        return new NameParts(
            parts[0],
            ""
        );
    }


    return new NameParts(
        parts[0],
        parts[1]
    );
}


private static String getUserIdFromLocation(
        String location
) {

    if (
        location == null ||
        location.isBlank()
    ) {
        return null;
    }


    String normalized =
        location.trim();

    while (
        normalized.endsWith("/")
    ) {

        normalized =
            normalized.substring(
                0,
                normalized.length() - 1
            );
    }


    int separator =
        normalized.lastIndexOf('/');


    if (
        separator < 0 ||
        separator ==
            normalized.length() - 1
    ) {
        return null;
    }


    return normalized.substring(
        separator + 1
    );
}


    private record NameParts(

    String firstName,
    String lastName

) {
}

    private record TokenResponse(

        @JsonProperty("access_token")
        String accessToken

    ) {
        
    }
}