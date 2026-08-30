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


    private record TokenResponse(

        @JsonProperty("access_token")
        String accessToken

    ) {
    }
}