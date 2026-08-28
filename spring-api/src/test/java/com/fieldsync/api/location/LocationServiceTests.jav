package com.fieldsync.api.location;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.context
    .SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.oauth2.server.resource.authentication
    .JwtAuthenticationToken;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
class LocationServiceTests {

    @Autowired
    private LocationService locationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TenantContextExecutor tenantContextExecutor;


    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder
            .clearContext();
    }


    @Test
    void shouldReturnOnlyActiveLocationsForAuthenticatedTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "location-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "location-b-" + suffix
            );

        String keycloakUserId =
            UUID.randomUUID()
                .toString();

        String email =
            "location-" +
            suffix +
            "@example.test";

        createUser(
            tenantA,
            "location-user-" + suffix,
            email,
            keycloakUserId,
            "user",
            true
        );

        createLocationAndReturnId(
            tenantA,
            "Tenant A First",
            true
        );

        createLocationAndReturnId(
            tenantA,
            "Tenant A Second",
            true
        );

        createLocationAndReturnId(
            tenantA,
            "Tenant A Inactive",
            false
        );

        createLocationAndReturnId(
            tenantB,
            "Tenant B Location",
            true
        );

        authenticate(
            keycloakUserId,
            email
        );

        List<LocationResponse> locations =
            locationService
                .getLocations();

        assertThat(locations)
            .extracting(
                LocationResponse::name
            )
            .containsExactly(
                "Tenant A Second",
                "Tenant A First"
            );

        assertThat(locations)
            .allMatch(
                location ->
                    location
                        .tenant_id()
                        .equals(tenantA)
            );
    }


    @Test
    void shouldCreateLocationForAuthenticatedTenant() {

        TestContext context =
            createAuthenticatedUser(
                "user",
                true
            );

        LocationResponse created =
            locationService.createLocation(
                new LocationRequest(
                    "  Colombo Office  ",
                    "Colombo 03"
                )
            );

        assertThat(created.name())
            .isEqualTo(
                "Colombo Office"
            );

        assertThat(created.address())
            .isEqualTo(
                "Colombo 03"
            );

        assertThat(created.tenant_id())
            .isEqualTo(
                context.tenantId()
            );

        assertThat(created.is_active())
            .isTrue();
    }


    @Test
    void shouldRejectLocationWithoutName() {

        createAuthenticatedUser(
            "user",
            true
        );

        assertThatThrownBy(
            () ->
                locationService.createLocation(
                    new LocationRequest(
                        "   ",
                        "Colombo"
                    )
                )
        )
        .isInstanceOf(
            LocationApiException.class
        )
        .hasMessage(
            "Location name is required"
        );
    }


    @Test
    void shouldUpdateLocation() {

        TestContext context =
            createAuthenticatedUser(
                "user",
                true
            );

        Integer locationId =
            createLocationAndReturnId(
                context.tenantId(),
                "Old Location",
                true
            );

        LocationResponse updated =
            locationService.updateLocation(
                locationId,
                new LocationRequest(
                    "Updated Location",
                    "Kandy"
                )
            );

        assertThat(updated.name())
            .isEqualTo(
                "Updated Location"
            );

        assertThat(updated.address())
            .isEqualTo(
                "Kandy"
            );
    }


    @Test
    void shouldRejectCreateWithoutWebAccess() {

        createAuthenticatedUser(
            "user",
            false
        );

        assertThatThrownBy(
            () ->
                locationService.createLocation(
                    new LocationRequest(
                        "Forbidden Location",
                        null
                    )
                )
        )
        .isInstanceOf(
            AccessDeniedException.class
        )
        .hasMessage(
            "Your account is not allowed to access the web application"
        );
    }


    @Test
    void shouldAllowAdminToDeleteLocation() {

        TestContext context =
            createAuthenticatedUser(
                "admin",
                true
            );

        Integer locationId =
            createLocationAndReturnId(
                context.tenantId(),
                "Delete Location",
                true
            );

        LocationDeleteResponse response =
            locationService.deleteLocation(
                locationId
            );

        assertThat(response.message())
            .isEqualTo(
                "Location deleted successfully"
            );

        assertThat(
            response.deletedLocation().id()
        )
        .isEqualTo(
            locationId
        );
    }


    @Test
    void shouldRejectDeleteForNonAdminUser() {

        TestContext context =
            createAuthenticatedUser(
                "user",
                true
            );

        Integer locationId =
            createLocationAndReturnId(
                context.tenantId(),
                "Protected Location",
                true
            );

        assertThatThrownBy(
            () ->
                locationService
                    .deleteLocation(
                        locationId
                    )
        )
        .isInstanceOf(
            LocationApiException.class
        )
        .hasMessage(
            "Admin access is required"
        );
    }


    private TestContext createAuthenticatedUser(
            String role,
            boolean accessWeb
    ) {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantId =
            createTenant(
                "location-crud-" + suffix
            );

        String keycloakUserId =
            UUID.randomUUID()
                .toString();

        String email =
            "location-crud-" +
            suffix +
            "@example.test";

        createUser(
            tenantId,
            "location-crud-user-" + suffix,
            email,
            keycloakUserId,
            role,
            accessWeb
        );

        authenticate(
            keycloakUserId,
            email
        );

        return new TestContext(
            tenantId,
            keycloakUserId,
            email
        );
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
            "Location Test Tenant",
            slug
        );
    }


    private void createUser(
            Integer tenantId,
            String username,
            String email,
            String keycloakUserId,
            String role,
            boolean accessWeb
    ) {

        jdbcTemplate.update(
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
                'Location Test User',
                ?,
                TRUE,
                ?,
                ?,
                FALSE,
                FALSE,
                ?,
                ?
            )
            """,

            username,
            role,
            email,
            accessWeb,
            tenantId,
            keycloakUserId
        );
    }


    private Integer createLocationAndReturnId(
            Integer tenantId,
            String name,
            boolean active
    ) {

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                jdbcTemplate.queryForObject(
                    """
                    INSERT INTO locations (
                        tenant_id,
                        name,
                        address,
                        is_active
                    )
                    VALUES (
                        ?,
                        ?,
                        'Test address',
                        ?
                    )
                    RETURNING id
                    """,
                    Integer.class,
                    tenantId,
                    name,
                    active
                )
        );
    }


    private void authenticate(
            String keycloakUserId,
            String email
    ) {

        Instant now =
            Instant.now();

        Jwt jwt =
            Jwt.withTokenValue(
                    "test-token"
                )
                .header(
                    "alg",
                    "RS256"
                )
                .subject(
                    keycloakUserId
                )
                .claim(
                    "email",
                    email
                )
                .claim(
                    "azp",
                    "fieldsync-web"
                )

                // Deliberately incorrect.
                // Tenant identity must come from PostgreSQL.
                .claim(
                    "tenantId",
                    999999
                )

                .issuedAt(now)
                .expiresAt(
                    now.plusSeconds(300)
                )
                .build();


        JwtAuthenticationToken authentication =
            new JwtAuthenticationToken(
                jwt,
                List.of()
            );


        SecurityContextHolder
            .getContext()
            .setAuthentication(
                authentication
            );
    }


    private record TestContext(

        Integer tenantId,
        String keycloakUserId,
        String email

    ) {
    }
}