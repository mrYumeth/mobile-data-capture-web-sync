package com.fieldsync.api.capturedrecord;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.oauth2.server.resource.authentication
    .JwtAuthenticationToken;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
class CapturedRecordReadTests {

    @Autowired
    private CapturedRecordService capturedRecordService;

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
    void shouldReturnOnlyAuthenticatedTenantRecordsNewestFirst() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "record-read-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "record-read-b-" + suffix
            );


        MasterData tenantAData =
            createMasterData(
                tenantA,
                "Tenant A"
            );

        MasterData tenantBData =
            createMasterData(
                tenantB,
                "Tenant B"
            );


        String keycloakUserId =
            UUID.randomUUID()
                .toString();

        String email =
            "record-read-" +
            suffix +
            "@example.test";


        createUser(
            tenantA,
            "record-user-" + suffix,
            email,
            keycloakUserId
        );


        LocalDateTime now =
            LocalDateTime.now();


        Integer olderRecordId =
            createCapturedRecord(
                tenantA,
                tenantAData,
                "Older Tenant A Record",
                now.minusHours(2),
                now.minusHours(1)
            );


        Integer newerRecordId =
            createCapturedRecord(
                tenantA,
                tenantAData,
                "Newest Tenant A Record",
                now.minusMinutes(30),
                now
            );


        createCapturedRecord(
            tenantB,
            tenantBData,
            "Tenant B Hidden Record",
            now,
            now.plusMinutes(1)
        );


        authenticate(
            keycloakUserId,
            email
        );


        List<CapturedRecordResponse> records =
            capturedRecordService
                .getCapturedRecords(
                    "http://localhost:8081"
                );


        assertThat(records)
            .hasSize(2);


        assertThat(records)
            .extracting(
                CapturedRecordResponse::id
            )
            .containsExactly(
                newerRecordId,
                olderRecordId
            );


        assertThat(records)
            .extracting(
                CapturedRecordResponse::description
            )
            .containsExactly(
                "Newest Tenant A Record",
                "Older Tenant A Record"
            );


        assertThat(records)
            .allMatch(
                record ->
                    record
                        .tenant_id()
                        .equals(tenantA)
            );


        CapturedRecordResponse newest =
            records.getFirst();


        assertThat(newest.customer_name())
            .isEqualTo(
                "Tenant A Customer"
            );

        assertThat(newest.location_name())
            .isEqualTo(
                "Tenant A Location"
            );

        assertThat(newest.category_name())
            .isEqualTo(
                "Tenant A Category"
            );
    }


    @Test
    void shouldReturnCapturedImagesInIdOrderWithFullUrls() {

        TestContext context =
            createAuthenticatedContext();


        Integer recordId =
            createCapturedRecord(
                context.tenantId(),
                context.masterData(),
                "Record With Images",
                LocalDateTime.now(),
                LocalDateTime.now()
            );


        createCapturedImage(
            context.tenantId(),
            recordId,
            "/uploads/captured-images/first.jpg"
        );

        createCapturedImage(
            context.tenantId(),
            recordId,
            "/uploads/captured-images/second.jpg"
        );


        CapturedRecordResponse record =
            capturedRecordService
                .getCapturedRecord(
                    recordId,
                    "http://localhost:8081"
                );


        assertThat(record.images())
            .hasSize(2);


        assertThat(record.images())
            .extracting(
                CapturedImageResponse::image_url
            )
            .containsExactly(
                "/uploads/captured-images/first.jpg",
                "/uploads/captured-images/second.jpg"
            );


        assertThat(record.images())
            .extracting(
                CapturedImageResponse::full_image_url
            )
            .containsExactly(
                "http://localhost:8081/uploads/captured-images/first.jpg",
                "http://localhost:8081/uploads/captured-images/second.jpg"
            );


        assertThat(record.full_image_url())
            .isEqualTo(
                "http://localhost:8081/uploads/captured-images/first.jpg"
            );
    }


    @Test
    void shouldNotRetrieveCapturedRecordFromAnotherTenant() {

        TestContext tenantA =
            createAuthenticatedContext();


        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");


        Integer tenantB =
            createTenant(
                "foreign-record-" + suffix
            );


        MasterData tenantBData =
            createMasterData(
                tenantB,
                "Foreign"
            );


        Integer foreignRecordId =
            createCapturedRecord(
                tenantB,
                tenantBData,
                "Foreign Tenant Record",
                LocalDateTime.now(),
                LocalDateTime.now()
            );


        assertThatThrownBy(
            () ->
                capturedRecordService
                    .getCapturedRecord(
                        foreignRecordId,
                        "http://localhost:8081"
                    )
        )
        .isInstanceOf(
            CapturedRecordApiException.class
        )
        .hasMessage(
            "Captured record not found"
        );


        assertThat(
            tenantA.tenantId()
        ).isNotEqualTo(
            tenantB
        );
    }


    @Test
    void shouldIgnoreTenantClaimFromJwtAndUseDatabaseTenant() {

        TestContext context =
            createAuthenticatedContext();


        Integer recordId =
            createCapturedRecord(
                context.tenantId(),
                context.masterData(),
                "Trusted Database Tenant",
                LocalDateTime.now(),
                LocalDateTime.now()
            );


        CapturedRecordResponse response =
            capturedRecordService
                .getCapturedRecord(
                    recordId,
                    "http://localhost:8081"
                );


        assertThat(response.tenant_id())
            .isEqualTo(
                context.tenantId()
            );


        assertThat(response.tenant_id())
            .isNotEqualTo(
                999999
            );
    }


    private TestContext createAuthenticatedContext() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");


        Integer tenantId =
            createTenant(
                "record-context-" + suffix
            );


        MasterData masterData =
            createMasterData(
                tenantId,
                "Context"
            );


        String keycloakUserId =
            UUID.randomUUID()
                .toString();


        String email =
            "record-context-" +
            suffix +
            "@example.test";


        createUser(
            tenantId,
            "record-context-user-" + suffix,
            email,
            keycloakUserId
        );


        authenticate(
            keycloakUserId,
            email
        );


        return new TestContext(
            tenantId,
            masterData
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
            "Captured Record Test Tenant",
            slug
        );
    }


    private MasterData createMasterData(
            Integer tenantId,
            String prefix
    ) {

        return tenantContextExecutor.execute(
            tenantId,
            () -> {

                Integer customerId =
                    jdbcTemplate.queryForObject(
                        """
                        INSERT INTO customers (
                            tenant_id,
                            name,
                            is_active
                        )
                        VALUES (?, ?, TRUE)
                        RETURNING id
                        """,
                        Integer.class,
                        tenantId,
                        prefix + " Customer"
                    );


                Integer locationId =
                    jdbcTemplate.queryForObject(
                        """
                        INSERT INTO locations (
                            tenant_id,
                            name,
                            is_active
                        )
                        VALUES (?, ?, TRUE)
                        RETURNING id
                        """,
                        Integer.class,
                        tenantId,
                        prefix + " Location"
                    );


                Integer categoryId =
                    jdbcTemplate.queryForObject(
                        """
                        INSERT INTO categories (
                            tenant_id,
                            name,
                            is_active
                        )
                        VALUES (?, ?, TRUE)
                        RETURNING id
                        """,
                        Integer.class,
                        tenantId,
                        prefix + " Category"
                    );


                return new MasterData(
                    customerId,
                    locationId,
                    categoryId
                );
            }
        );
    }


    private void createUser(
            Integer tenantId,
            String username,
            String email,
            String keycloakUserId
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
                'Captured Record Test User',
                'user',
                TRUE,
                ?,
                TRUE,
                FALSE,
                FALSE,
                ?,
                ?
            )
            """,
            username,
            email,
            tenantId,
            keycloakUserId
        );
    }


    private Integer createCapturedRecord(
            Integer tenantId,
            MasterData masterData,
            String description,
            LocalDateTime capturedAt,
            LocalDateTime receivedAt
    ) {

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                jdbcTemplate.queryForObject(
                    """
                    INSERT INTO captured_records (
                        tenant_id,
                        customer_id,
                        location_id,
                        category_id,
                        description,
                        latitude,
                        longitude,
                        captured_at,
                        received_at
                    )
                    VALUES (
                        ?,
                        ?,
                        ?,
                        ?,
                        ?,
                        6.9270790,
                        79.8612440,
                        ?,
                        ?
                    )
                    RETURNING id
                    """,
                    Integer.class,

                    tenantId,
                    masterData.customerId(),
                    masterData.locationId(),
                    masterData.categoryId(),

                    description,

                    capturedAt,
                    receivedAt
                )
        );
    }


    private Integer createCapturedImage(
            Integer tenantId,
            Integer recordId,
            String imageUrl
    ) {

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                jdbcTemplate.queryForObject(
                    """
                    INSERT INTO captured_images (
                        captured_record_id,
                        tenant_id,
                        image_url,
                        storage_path
                    )
                    VALUES (
                        ?,
                        ?,
                        ?,
                        NULL
                    )
                    RETURNING id
                    """,
                    Integer.class,
                    recordId,
                    tenantId,
                    imageUrl
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

                // Deliberately wrong.
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


    private record MasterData(

        Integer customerId,
        Integer locationId,
        Integer categoryId

    ) {
    }


    private record TestContext(

        Integer tenantId,
        MasterData masterData

    ) {
    }
}