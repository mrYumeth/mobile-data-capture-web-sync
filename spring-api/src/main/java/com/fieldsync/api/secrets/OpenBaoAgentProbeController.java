package com.fieldsync.api.secrets;

import org.springframework.context.annotation.Profile;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@Profile("local")
@RestController
@RequestMapping("/api/dev/openbao")
public class OpenBaoAgentProbeController {

    private final OpenBaoRenderedSecretService
        secretService;

    public OpenBaoAgentProbeController(
            OpenBaoRenderedSecretService secretService
    ) {
        this.secretService =
            secretService;
    }

    @GetMapping("/verification")
    public Map<String, Object> verification() {

        String value =
            secretService
                .readVerificationSecret();

        String[] parts =
            value.split("\\|", 2);

        Map<String, Object> response =
            new LinkedHashMap<>();

        response.put(
            "source",
            "openbao-agent"
        );

        response.put(
            "application",
            parts.length > 0
                ? parts[0]
                : null
        );

        response.put(
            "environment",
            parts.length > 1
                ? parts[1]
                : null
        );

        return response;
    }
}