package com.fieldsync.api.security;

import org.springframework.context.annotation.Profile;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Profile("local")
@RestController
@RequestMapping("/api/dev/security")
public class SecurityProbeController {

    @GetMapping("/ping")
    public Map<String, Object> ping(
            @AuthenticationPrincipal Jwt jwt
    ) {

        Map<String, Object> response =
            new LinkedHashMap<>();

        response.put(
            "status",
            "authenticated"
        );

        response.put(
            "subject",
            jwt.getSubject()
        );

        response.put(
            "authorizedParty",
            jwt.getClaimAsString("azp")
        );

        response.put(
            "issuer",
            jwt.getIssuer().toString()
        );

        return response;
    }
}