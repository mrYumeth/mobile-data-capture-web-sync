package com.fieldsync.api.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Supplier;

@Component
public class TenantContextExecutor {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public <T> T execute(
            Integer tenantId,
            Supplier<T> operation
    ) {

        if (tenantId == null || tenantId <= 0) {
            throw new IllegalArgumentException(
                "Tenant ID is required"
            );
        }

        entityManager
            .createNativeQuery(
                """
                SELECT set_config(
                    'app.current_tenant_id',
                    :tenantId,
                    true
                )
                """
            )
            .setParameter(
                "tenantId",
                tenantId.toString()
            )
            .getSingleResult();

        return operation.get();
    }

    @Transactional
    public void execute(
            Integer tenantId,
            Runnable operation
    ) {

        execute(
            tenantId,
            () -> {
                operation.run();
                return null;
            }
        );
    }
}