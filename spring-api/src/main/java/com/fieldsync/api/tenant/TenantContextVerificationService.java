package com.fieldsync.api.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Service;

@Service
public class TenantContextVerificationService {

    private final TenantContextExecutor tenantContextExecutor;

    @PersistenceContext
    private EntityManager entityManager;

    public TenantContextVerificationService(
            TenantContextExecutor tenantContextExecutor
    ) {
        this.tenantContextExecutor =
            tenantContextExecutor;
    }

    public String verify(Integer tenantId) {

        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                Object result = entityManager
                    .createNativeQuery(
                        """
                        SELECT current_setting(
                            'app.current_tenant_id',
                            true
                        )
                        """
                    )
                    .getSingleResult();

                return result == null
                    ? null
                    : result.toString();
            }
        );
    }
}