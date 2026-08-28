package com.fieldsync.api.database;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
class CapturedRecordRowLevelSecurityTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TenantContextExecutor tenantContextExecutor;


    @Test
    void shouldOnlyExposeCapturedRecordsForCurrentTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "record-rls-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "record-rls-b-" + suffix
            );

        String tenantADescription =
            "Tenant A Record " + suffix;

        String tenantBDescription =
            "Tenant B Record " + suffix;


        createCapturedRecord(
            tenantA,
            tenantADescription
        );

        createCapturedRecord(
            tenantB,
            tenantBDescription
        );


        List<String> descriptions =
            tenantContextExecutor.execute(
                tenantA,
                () ->
                    jdbcTemplate.queryForList(
                        """
                        SELECT description
                        FROM captured_records
                        WHERE description IN (?, ?)
                        ORDER BY description
                        """,
                        String.class,
                        tenantADescription,
                        tenantBDescription
                    )
            );


        assertThat(descriptions)
            .containsExactly(
                tenantADescription
            );
    }


    @Test
    void shouldOnlyExposeCapturedImagesForCurrentTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "image-rls-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "image-rls-b-" + suffix
            );


        Integer recordA =
            createCapturedRecord(
                tenantA,
                "Tenant A Image Record " +
                suffix
            );

        Integer recordB =
            createCapturedRecord(
                tenantB,
                "Tenant B Image Record " +
                suffix
            );


        String tenantAPath =
            "tenant-a/" +
            suffix +
            ".jpg";

        String tenantBPath =
            "tenant-b/" +
            suffix +
            ".jpg";


        createCapturedImage(
            tenantA,
            recordA,
            tenantAPath
        );

        createCapturedImage(
            tenantB,
            recordB,
            tenantBPath
        );


        List<String> paths =
            tenantContextExecutor.execute(
                tenantA,
                () ->
                    jdbcTemplate.queryForList(
                        """
                        SELECT storage_path
                        FROM captured_images
                        WHERE storage_path IN (?, ?)
                        ORDER BY storage_path
                        """,
                        String.class,
                        tenantAPath,
                        tenantBPath
                    )
            );


        assertThat(paths)
            .containsExactly(
                tenantAPath
            );
    }


    @Test
    void shouldRejectCrossTenantCapturedRecordInsert() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "record-write-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "record-write-b-" + suffix
            );


        assertThatThrownBy(
            () ->
                tenantContextExecutor.execute(
                    tenantA,
                    () ->
                        jdbcTemplate.update(
                            """
                            INSERT INTO captured_records (
                                tenant_id,
                                description
                            )
                            VALUES (?, ?)
                            """,
                            tenantB,
                            "Forbidden Record " +
                            suffix
                        )
                )
        )
        .isInstanceOf(
            DataAccessException.class
        );
    }


    @Test
    void shouldRejectCrossTenantCapturedImageInsert() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "image-write-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "image-write-b-" + suffix
            );


        Integer tenantBRecord =
            createCapturedRecord(
                tenantB,
                "Tenant B Protected Record " +
                suffix
            );


        assertThatThrownBy(
            () ->
                tenantContextExecutor.execute(
                    tenantA,
                    () ->
                        jdbcTemplate.update(
                            """
                            INSERT INTO captured_images (
                                captured_record_id,
                                tenant_id,
                                storage_path
                            )
                            VALUES (?, ?, ?)
                            """,
                            tenantBRecord,
                            tenantB,
                            "forbidden/" +
                            suffix +
                            ".jpg"
                        )
                )
        )
        .isInstanceOf(
            DataAccessException.class
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
            "Captured Record RLS Tenant",
            slug
        );
    }


    private Integer createCapturedRecord(
            Integer tenantId,
            String description
    ) {

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                jdbcTemplate.queryForObject(
                    """
                    INSERT INTO captured_records (
                        tenant_id,
                        description
                    )
                    VALUES (?, ?)
                    RETURNING id
                    """,
                    Integer.class,
                    tenantId,
                    description
                )
        );
    }


    private Integer createCapturedImage(
            Integer tenantId,
            Integer capturedRecordId,
            String storagePath
    ) {

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                jdbcTemplate.queryForObject(
                    """
                    INSERT INTO captured_images (
                        captured_record_id,
                        tenant_id,
                        storage_path
                    )
                    VALUES (?, ?, ?)
                    RETURNING id
                    """,
                    Integer.class,
                    capturedRecordId,
                    tenantId,
                    storagePath
                )
        );
    }
}