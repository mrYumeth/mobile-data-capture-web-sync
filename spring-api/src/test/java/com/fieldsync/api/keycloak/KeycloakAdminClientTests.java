package com.fieldsync.api.keycloak;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import org.springframework.test.web.client.MockRestServiceServer;

import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;

import static org.springframework.test.web.client.response.MockRestResponseCreators.*;


class KeycloakAdminClientTests {

    @Test
    void verifiesUserManagementAccessUsingServiceAccount() {

        RestClient.Builder builder =
            RestClient.builder();

        MockRestServiceServer server =
            MockRestServiceServer
                .bindTo(builder)
                .build();


        KeycloakAdminClient client =
            new KeycloakAdminClient(
                builder,
                "http://localhost:8080",
                "fieldsync",
                "fieldsync-backend-admin",
                "test-secret"
            );


        server.expect(
                requestTo(
                    "http://localhost:8080/realms/fieldsync/protocol/openid-connect/token"
                )
            )
            .andExpect(
                method(
                    HttpMethod.POST
                )
            )
            .andRespond(
                withSuccess(
                    """
                    {
                      "access_token": "admin-test-token"
                    }
                    """,
                    MediaType.APPLICATION_JSON
                )
            );


        server.expect(
                requestTo(
                    "http://localhost:8080/admin/realms/fieldsync/users?max=1"
                )
            )
            .andExpect(
                method(
                    HttpMethod.GET
                )
            )
            .andExpect(
                header(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer admin-test-token"
                )
            )
            .andRespond(
                withSuccess(
                    "[]",
                    MediaType.APPLICATION_JSON
                )
            );


        assertTrue(
            client.verifyUserManagementAccess()
        );

        server.verify();
    }
}