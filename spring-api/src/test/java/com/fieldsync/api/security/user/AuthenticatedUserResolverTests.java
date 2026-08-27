package com.fieldsync.api.security.user;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class AuthenticatedUserResolverTests {

    @Autowired
    private AuthenticatedUserResolver resolver;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldResolveTenantFromDatabase() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantId =
            createTenant(
                "tenant-" + suffix
            );

        String keycloakUserId =
            UUID.randomUUID()
                .toString();

        String email =
            "user-" + suffix
                + "@example.test";

        createUser(
            tenantId,
            "user-" + suffix,
            email,
            keycloakUserId,
            true,
            true,
            true
        );

        Jwt jwt =
            createJwt(
                keycloakUserId,
                email,
                "fieldsync-web"
            );

        AuthenticatedFieldSyncUser user =
            resolver.resolve(jwt);

        assertThat(
            user.tenantId()
        ).isEqualTo(tenantId);

        assertThat(
            user.clientType()
        ).isEqualTo("web");
    }

    @Test
    void shouldRejectUnauthorizedWebAccess() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantId =
            createTenant(
                "tenant-" + suffix
            );

        String keycloakUserId =
            UUID.randomUUID()
                .toString();

        String email =
            "mobile-" + suffix
                + "@example.test";

        createUser(
            tenantId,
            "mobile-" + suffix,
            email,
            keycloakUserId,
            true,
            false,
            true
        );

        Jwt jwt =
            createJwt(
                keycloakUserId,
                email,
                "fieldsync-web"
            );

        assertThatThrownBy(
            () -> resolver.resolve(jwt)
        )
        .isInstanceOf(
            AccessDeniedException.class
        );
    }

    @Test
    void shouldLinkUserByEmailWhenNeeded() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantId =
            createTenant(
                "tenant-" + suffix
            );

        String email =
            "link-" + suffix
                + "@example.test";

        createUser(
            tenantId,
            "link-" + suffix,
            email,
            null,
            true,
            true,
            true
        );

        String newKeycloakUserId =
            UUID.randomUUID()
                .toString();

        Jwt jwt =
            createJwt(
                newKeycloakUserId,
                email,
                "fieldsync-web"
            );

        AuthenticatedFieldSyncUser user =
            resolver.resolve(jwt);

        assertThat(
            user.keycloakUserId()
        ).isEqualTo(
            newKeycloakUserId
        );

        Integer count =
            jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM users
                WHERE keycloak_user_id = ?
                """,
                Integer.class,
                newKeycloakUserId
            );

        assertThat(count)
            .isEqualTo(1);
    }

    private Integer createTenant(
            String slug
    ) {

        return jdbcTemplate.queryForObject(
            """
            INSERT INTO tenants (
                name,
                slug
            )
            VALUES (?, ?)
            RETURNING id
            """,
            Integer.class,
            "Test Tenant",
            slug
        );
    }

    private Integer createUser(
            Integer tenantId,
            String username,
            String email,
            String keycloakUserId,
            boolean active,
            boolean accessWeb,
            boolean accessMobile
    ) {

        return jdbcTemplate.queryForObject(
            """
            INSERT INTO users (
                username,
                password_hash,
                full_name,
                role,
                is_active,
                email,
                access_web,
                access_mobile,
                password_change_required,
                tenant_id,
                keycloak_user_id
            )
            VALUES (
                ?,
                'test-only-placeholder',
                'Test User',
                'mobile_user',
                ?,
                ?,
                ?,
                ?,
                FALSE,
                ?,
                ?
            )
            RETURNING id
            """,
            Integer.class,

            username,
            active,
            email,
            accessWeb,
            accessMobile,
            tenantId,
            keycloakUserId
        );
    }

    private Jwt createJwt(
            String subject,
            String email,
            String authorizedParty
    ) {

        Instant now =
            Instant.now();

        return Jwt
            .withTokenValue(
                "test-token"
            )
            .header(
                "alg",
                "RS256"
            )
            .subject(subject)
            .claim(
                "email",
                email
            )
            .claim(
                "azp",
                authorizedParty
            )

            // Intentionally wrong.
            // Resolver must ignore it.
            .claim(
                "tenantId",
                999999
            )

            .issuedAt(now)
            .expiresAt(
                now.plusSeconds(300)
            )
            .build();
    }
}