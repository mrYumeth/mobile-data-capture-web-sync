package com.fieldsync.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import org.springframework.http.HttpMethod;

import java.util.List;


@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http

            .cors(
                Customizer.withDefaults()
            )

            .csrf(
                csrf ->
                    csrf.disable()
            )

            .sessionManagement(
                session ->
                    session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS
                    )
            )

            .authorizeHttpRequests(
                authorize ->
                    authorize

                        .requestMatchers(
                            HttpMethod.POST,
                            "/api/auth/register-tenant"
                        )
                        .permitAll()

                        .requestMatchers(
                            "/actuator/health",
                            "/actuator/info",
                            "/uploads/**"
                        )
                        .permitAll()

                        .anyRequest()
                        .authenticated()
            )

            .oauth2ResourceServer(
                oauth2 ->
                    oauth2.jwt(
                        Customizer.withDefaults()
                    )
            );

        return http.build();
    }


    @Bean
    CorsConfigurationSource
    corsConfigurationSource() {

        CorsConfiguration configuration =
            new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of(
                "http://localhost:5173"
            )
        );

        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );

        configuration.setAllowCredentials(
            true
        );


        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            configuration
        );

        return source;
    }
}