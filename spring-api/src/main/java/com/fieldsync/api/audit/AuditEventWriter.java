package com.fieldsync.api.audit;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;

@Component
public class AuditEventWriter {

    @PersistenceContext
    private EntityManager entityManager;


    public void recordUserCreated(
            Integer tenantId,
            Integer actorUserId,
            Integer targetUserId,
            String username,
            boolean accessWeb,
            boolean accessMobile
    ) {

        String details =
            "username=" + username
            + ", accessWeb=" + accessWeb
            + ", accessMobile=" + accessMobile;


        entityManager
            .createNativeQuery(
                """
                INSERT INTO audit_events (
                    tenant_id,
                    actor_user_id,
                    action,
                    entity_type,
                    entity_id,
                    outcome,
                    details
                )
                VALUES (
                    :tenantId,
                    :actorUserId,
                    'USER_CREATED',
                    'USER',
                    :entityId,
                    'SUCCESS',
                    :details
                )
                """
            )
            .setParameter(
                "tenantId",
                tenantId
            )
            .setParameter(
                "actorUserId",
                actorUserId
            )
            .setParameter(
                "entityId",
                targetUserId.toString()
            )
            .setParameter(
                "details",
                details
            )
            .executeUpdate();
    }
}