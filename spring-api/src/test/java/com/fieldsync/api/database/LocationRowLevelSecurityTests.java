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
class LocationRowLevelSecurityTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TenantContextExecutor tenantContextExecutor;


    @Test
    void shouldOnlyExposeLocationsForCurrentDatabaseTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "location-rls-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "location-rls-b-" + suffix
            );

        String tenantAName =
            "Location RLS A " + suffix;

        String tenantBName =
            "Location RLS B " + suffix;


        createLocation(
            tenantA,
            tenantAName
        );

        createLocation(
            tenantB,
            tenantBName
        );


        List<String> names =
            tenantContextExecutor.execute(
                tenantA,
                () ->
                    jdbcTemplate.queryForList(
                        """
                        SELECT name
                        FROM locations
                        WHERE name IN (?, ?)
                        ORDER BY name
                        """,
                        String.class,
                        tenantAName,
                        tenantBName
                    )
            );


        assertThat(names)
            .containsExactly(
                tenantAName
            );
    }


    @Test
    void shouldRejectCrossTenantLocationInsert() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "location-write-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "location-write-b-" + suffix
            );


        assertThatThrownBy(
            () ->
                tenantContextExecutor.execute(
                    tenantA,
                    () ->
                        jdbcTemplate.update(
                            """
                            INSERT INTO locations (
                                tenant_id,
                                name,
                                is_active
                            )
                            VALUES (
                                ?,
                                ?,
                                TRUE
                            )
                            """,
                            tenantB,
                            "Forbidden Location " +
                            suffix
                        )
                )
        )
        .isInstanceOf(
            DataAccessException.class
        );
    }


    @Test
    void shouldNotUpdateLocationFromAnotherTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "location-update-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "location-update-b-" + suffix
            );

        Integer tenantBLocation =
            createLocation(
                tenantB,
                "Tenant B Protected " +
                suffix
            );


        Integer updatedRows =
            tenantContextExecutor.execute(
                tenantA,
                () ->
                    jdbcTemplate.update(
                        """
                        UPDATE locations
                        SET name = ?
                        WHERE id = ?
                        """,
                        "Illegal Update",
                        tenantBLocation
                    )
            );


        assertThat(updatedRows)
            .isZero();
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
            "Location RLS Tenant",
            slug
        );
    }


    private Integer createLocation(
            Integer tenantId,
            String name
    ) {

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                jdbcTemplate.queryForObject(
                    """
                    INSERT INTO locations (
                        tenant_id,
                        name,
                        is_active
                    )
                    VALUES (
                        ?,
                        ?,
                        TRUE
                    )
                    RETURNING id
                    """,
                    Integer.class,
                    tenantId,
                    name
                )
        );
    }
}