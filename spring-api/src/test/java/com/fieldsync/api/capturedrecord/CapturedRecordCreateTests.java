package com.fieldsync.api.capturedrecord;

import com.fieldsync.api.storage.ImageStorageService;
import com.fieldsync.api.storage.StoredImage;
import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.mock.web.MockMultipartFile;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.oauth2.server.resource.authentication
    .JwtAuthenticationToken;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

import java.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@SpringBootTest
@Transactional
class CapturedRecordCreateTests {

    @Autowired
    private CapturedRecordService
        capturedRecordService;

    @Autowired
    private JdbcTemplate
        jdbcTemplate;

    @Autowired
    private TenantContextExecutor
        tenantContextExecutor;


    @MockitoBean
    private ImageStorageService
        imageStorageService;


    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder
            .clearContext();
    }


    @Test
    void shouldCreateFlutterCompatibleCapturedRecord() {

        TestContext context =
            createAuthenticatedMobileContext();


        CapturedRecordCreateResponse response =
            capturedRecordService
                .createCapturedRecord(
                    createRequest(
                        context.masterData(),
                        "Flutter field record",
                        "6.9270790",
                        "79.8612440",
                        "2026-08-29T09:30:00"
                    ),
                    List.of(),
                    null,
                    "http://localhost:8081"
                );


        assertThat(response.message())
            .isEqualTo(
                "Captured record created successfully"
            );


        assertThat(response.record())
            .isNotNull();


        assertThat(response.record().id())
            .isNotNull()
            .isPositive();


        assertThat(response.record().tenant_id())
            .isEqualTo(
                context.tenantId()
            );


        assertThat(response.record().description())
            .isEqualTo(
                "Flutter field record"
            );


        assertThat(response.record().customer_name())
            .isEqualTo(
                "Mobile Customer"
            );


        assertThat(response.record().location_name())
            .isEqualTo(
                "Mobile Location"
            );


        assertThat(response.record().category_name())
            .isEqualTo(
                "Mobile Category"
            );


        assertThat(response.record().latitude())
            .isEqualByComparingTo(
                new BigDecimal(
                    "6.9270790"
                )
            );


        assertThat(response.record().longitude())
            .isEqualByComparingTo(
                new BigDecimal(
                    "79.8612440"
                )
            );


        assertThat(response.record().images())
            .isEmpty();


        Integer storedTenantId =
            tenantContextExecutor.execute(
                context.tenantId(),
                () ->
                    jdbcTemplate.queryForObject(
                        """
                        SELECT tenant_id
                        FROM captured_records
                        WHERE id = ?
                        """,
                        Integer.class,
                        response.record().id()
                    )
            );


        assertThat(storedTenantId)
            .isEqualTo(
                context.tenantId()
            );
    }


    @Test
    void shouldCreateCapturedRecordWithMultipleImages() {

        TestContext context =
            createAuthenticatedMobileContext();


        MockMultipartFile firstImage =
            new MockMultipartFile(
                "images",
                "first.jpg",
                "image/jpeg",
                new byte[] {
                    1,
                    2,
                    3
                }
            );


        MockMultipartFile secondImage =
            new MockMultipartFile(
                "images",
                "second.jpg",
                "image/jpeg",
                new byte[] {
                    4,
                    5,
                    6
                }
            );


        when(
            imageStorageService.store(
                any(MultipartFile.class),
                eq(context.tenantId())
            )
        )
        .thenReturn(
            new StoredImage(
                "/uploads/captured-images/first.jpg",
                "test/first.jpg"
            ),
            new StoredImage(
                "/uploads/captured-images/second.jpg",
                "test/second.jpg"
            )
        );


        when(
            imageStorageService.resolveImageUrl(
                anyString(),
                anyString(),
                eq("http://localhost:8081")
            )
        )
        .thenAnswer(
            invocation -> {

                String imageUrl =
                    invocation.getArgument(0);

                return "http://localhost:8081"
                    + imageUrl;
            }
        );


        CapturedRecordCreateResponse response =
            capturedRecordService
                .createCapturedRecord(
                    createRequest(
                        context.masterData(),
                        "Multiple images",
                        null,
                        null,
                        null
                    ),
                    List.of(
                        firstImage,
                        secondImage
                    ),
                    null,
                    "http://localhost:8081"
                );


        assertThat(response.record().id())
            .isNotNull();


        assertThat(response.record().images())
            .hasSize(2);


        assertThat(response.record().images())
            .extracting(
                CapturedImageResponse::image_url
            )
            .containsExactly(
                "/uploads/captured-images/first.jpg",
                "/uploads/captured-images/second.jpg"
            );


        assertThat(response.record().image_url())
            .isEqualTo(
                "/uploads/captured-images/first.jpg"
            );


        assertThat(response.record().image_path())
            .isEqualTo(
                "test/first.jpg"
            );


        Integer imageCount =
            tenantContextExecutor.execute(
                context.tenantId(),
                () ->
                    jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)::INTEGER
                        FROM captured_images
                        WHERE captured_record_id = ?
                        """,
                        Integer.class,
                        response.record().id()
                    )
            );


        assertThat(imageCount)
            .isEqualTo(2);


        verify(
            imageStorageService,
            times(2)
        )
        .store(
            any(MultipartFile.class),
            eq(context.tenantId())
        );
    }


    @Test
    void shouldSupportLegacySingleImageField() {

        TestContext context =
            createAuthenticatedMobileContext();


        MockMultipartFile legacyImage =
            new MockMultipartFile(
                "image",
                "legacy.jpg",
                "image/jpeg",
                new byte[] {
                    7,
                    8,
                    9
                }
            );


        when(
            imageStorageService.store(
                any(MultipartFile.class),
                eq(context.tenantId())
            )
        )
        .thenReturn(
            new StoredImage(
                "/uploads/captured-images/legacy.jpg",
                "test/legacy.jpg"
            )
        );


        when(
            imageStorageService.resolveImageUrl(
                anyString(),
                anyString(),
                eq("http://localhost:8081")
            )
        )
        .thenReturn(
            "http://localhost:8081/uploads/captured-images/legacy.jpg"
        );


        CapturedRecordCreateResponse response =
            capturedRecordService
                .createCapturedRecord(
                    createRequest(
                        context.masterData(),
                        "Legacy image",
                        null,
                        null,
                        null
                    ),
                    List.of(),
                    legacyImage,
                    "http://localhost:8081"
                );


        assertThat(response.record().images())
            .hasSize(1);


        assertThat(
            response
                .record()
                .images()
                .getFirst()
                .image_url()
        )
        .isEqualTo(
            "/uploads/captured-images/legacy.jpg"
        );


        verify(
            imageStorageService,
            times(1)
        )
        .store(
            any(MultipartFile.class),
            eq(context.tenantId())
        );
    }


    @Test
    void shouldRejectReferencesFromAnotherTenantBeforeUploadingImages() {

        TestContext tenantA =
            createAuthenticatedMobileContext();


        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");


        Integer tenantB =
            createTenant(
                "foreign-upload-" + suffix
            );


        MasterData tenantBData =
            createMasterData(
                tenantB,
                "Foreign"
            );


        CapturedRecordCreateRequest request =
            new CapturedRecordCreateRequest(
                tenantBData
                    .customerId()
                    .toString(),

                tenantA
                    .masterData()
                    .locationId()
                    .toString(),

                tenantA
                    .masterData()
                    .categoryId()
                    .toString(),

                "Cross tenant attempt",

                null,
                null,
                null
            );


        MockMultipartFile image =
            new MockMultipartFile(
                "images",
                "forbidden.jpg",
                "image/jpeg",
                new byte[] {
                    1
                }
            );


        assertThatThrownBy(
            () ->
                capturedRecordService
                    .createCapturedRecord(
                        request,
                        List.of(image),
                        null,
                        "http://localhost:8081"
                    )
        )
        .isInstanceOf(
            CapturedRecordApiException.class
        )
        .hasMessage(
            "Selected customer, location, or category does not belong to your tenant"
        );


        verifyNoInteractions(
            imageStorageService
        );
    }


    @Test
    void shouldRejectInvalidGpsValues() {

        TestContext context =
            createAuthenticatedMobileContext();


        CapturedRecordCreateRequest request =
            createRequest(
                context.masterData(),
                "Invalid GPS",
                "not-a-number",
                "79.8612440",
                null
            );


        assertThatThrownBy(
            () ->
                capturedRecordService
                    .createCapturedRecord(
                        request,
                        List.of(),
                        null,
                        "http://localhost:8081"
                    )
        )
        .isInstanceOf(
            CapturedRecordApiException.class
        )
        .hasMessage(
            "Latitude and longitude must be valid numbers"
        );


        verifyNoInteractions(
            imageStorageService
        );
    }


    @Test
    void shouldRejectInvalidCapturedDateTime() {

        TestContext context =
            createAuthenticatedMobileContext();


        CapturedRecordCreateRequest request =
            createRequest(
                context.masterData(),
                "Invalid time",
                null,
                null,
                "not-a-date"
            );


        assertThatThrownBy(
            () ->
                capturedRecordService
                    .createCapturedRecord(
                        request,
                        List.of(),
                        null,
                        "http://localhost:8081"
                    )
        )
        .isInstanceOf(
            CapturedRecordApiException.class
        )
        .hasMessage(
            "Captured date/time is invalid"
        );


        verifyNoInteractions(
            imageStorageService
        );
    }


    @Test
    void shouldRejectMoreThanTenImages() {

        TestContext context =
            createAuthenticatedMobileContext();


        List<MultipartFile> images =
            new ArrayList<>();


        for (int index = 0; index < 11; index++) {

            images.add(
                new MockMultipartFile(
                    "images",
                    "image-" + index + ".jpg",
                    "image/jpeg",
                    new byte[] {
                        1
                    }
                )
            );
        }


        assertThatThrownBy(
            () ->
                capturedRecordService
                    .createCapturedRecord(
                        createRequest(
                            context.masterData(),
                            "Too many images",
                            null,
                            null,
                            null
                        ),
                        images,
                        null,
                        "http://localhost:8081"
                    )
        )
        .isInstanceOf(
            CapturedRecordApiException.class
        )
        .hasMessage(
            "A maximum of 10 images is allowed"
        );


        verifyNoInteractions(
            imageStorageService
        );
    }


    private TestContext
    createAuthenticatedMobileContext() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");


        Integer tenantId =
            createTenant(
                "mobile-upload-" + suffix
            );


        MasterData masterData =
            createMasterData(
                tenantId,
                "Mobile"
            );


        String keycloakUserId =
            UUID.randomUUID()
                .toString();


        String email =
            "mobile-upload-"
                + suffix
                + "@example.test";


        createMobileUser(
            tenantId,
            "mobile-upload-user-" + suffix,
            email,
            keycloakUserId
        );


        authenticateMobile(
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
            "Captured Upload Tenant",
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


    private void createMobileUser(
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
                'Mobile Upload Test User',
                'mobile_user',
                TRUE,
                ?,
                FALSE,
                TRUE,
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


    private CapturedRecordCreateRequest
    createRequest(
            MasterData data,
            String description,
            String latitude,
            String longitude,
            String capturedAt
    ) {

        return new CapturedRecordCreateRequest(

            data.customerId()
                .toString(),

            data.locationId()
                .toString(),

            data.categoryId()
                .toString(),

            description,
            latitude,
            longitude,
            capturedAt
        );
    }


    private void authenticateMobile(
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
                    "fieldsync-mobile"
                )

                // Deliberately incorrect.
                // PostgreSQL user tenant must win.
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