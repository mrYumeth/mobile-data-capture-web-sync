package com.fieldsync.api.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class JwtSecurityConfig {

    private final String issuer;
    private final String audience;

    public JwtSecurityConfig(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            String issuer,

            @Value("${fieldsync.security.jwt.audience:fieldsync-api}")
            String audience
    ) {
        this.issuer = issuer;
        this.audience = audience;
    }

    @Bean
    JwtDecoder jwtDecoder() {

        NimbusJwtDecoder decoder =
            NimbusJwtDecoder
                .withIssuerLocation(issuer)
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> standardValidator =
            JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator =
            new JwtAudienceValidator(audience);

        OAuth2TokenValidator<Jwt> subjectValidator =
            new JwtClaimValidator<String>(
                "sub",
                subject ->
                    subject != null &&
                    !subject.isBlank()
            );

        OAuth2TokenValidator<Jwt> tokenPurposeValidator =
            new JwtClaimValidator<String>(
                "typ",
                "Bearer"::equals
            );

        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                standardValidator,
                audienceValidator,
                subjectValidator,
                tokenPurposeValidator
            )
        );

        return decoder;
    }
}