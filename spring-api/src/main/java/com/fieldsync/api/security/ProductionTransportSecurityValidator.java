package com.fieldsync.api.security;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@Profile("prod")
public class ProductionTransportSecurityValidator
        implements InitializingBean {

    private final String keycloakIssuerUri;
    private final String keycloakAdminBaseUrl;
    private final String webOrigin;

    public ProductionTransportSecurityValidator(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            String keycloakIssuerUri,

            @Value("${fieldsync.keycloak.admin.base-url}")
            String keycloakAdminBaseUrl,

            @Value("${fieldsync.web.allowed-origin}")
            String webOrigin
    ) {
        this.keycloakIssuerUri = keycloakIssuerUri;
        this.keycloakAdminBaseUrl = keycloakAdminBaseUrl;
        this.webOrigin = webOrigin;
    }

    @Override
    public void afterPropertiesSet() {
        requireHttps(
            keycloakIssuerUri,
            "Keycloak issuer URI"
        );

        requireHttps(
            keycloakAdminBaseUrl,
            "Keycloak admin base URL"
        );

        requireHttps(
            webOrigin,
            "FieldSync web origin"
        );
    }

    private void requireHttps(
            String value,
            String name
    ) {
        try {
            URI uri = URI.create(value);

            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null) {

                throw new IllegalStateException(
                    name +
                    " must use a valid HTTPS URL in production"
                );
            }

        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                name +
                " must use a valid HTTPS URL in production",
                exception
            );
        }
    }
}