package com.fieldsync.api.tenant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TenantContextExecutorTests {

    @Autowired
    private TenantContextVerificationService
        verificationService;

    @Test
    void shouldApplyTenantContextInsideTransaction() {

        String tenantId =
            verificationService.verify(42);

        assertThat(tenantId)
            .isEqualTo("42");
    }
}