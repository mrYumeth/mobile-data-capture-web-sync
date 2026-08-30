package com.fieldsync.api.keycloak;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.client.RestClient;


@Configuration
public class KeycloakAdminHttpConfig {

    @Bean(
        name = "keycloakAdminRestClientBuilder"
    )
    public RestClient.Builder
    keycloakAdminRestClientBuilder() {

        return RestClient.builder();
    }
}