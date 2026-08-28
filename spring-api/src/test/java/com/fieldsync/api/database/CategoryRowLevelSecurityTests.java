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
class CategoryRowLevelSecurityTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TenantContextExecutor tenantContextExecutor;


    @Test
    void shouldOnlyExposeCategoriesForCurrentDatabaseTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "rls-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "rls-b-" + suffix
            );

        String tenantAName =
            "RLS Tenant A " + suffix;

        String tenantBName =
            "RLS Tenant B " + suffix;


        createCategory(
            tenantA,
            tenantAName
        );

        createCategory(
            tenantB,
            tenantBName
        );


        List<String> visibleNames =
            tenantContextExecutor.execute(
                tenantA,
                () ->
                    jdbcTemplate.queryForList(
                        """
                        SELECT name
                        FROM categories
                        WHERE name IN (?, ?)
                        ORDER BY name
                        """,
                        String.class,
                        tenantAName,
                        tenantBName
                    )
            );


        assertThat(visibleNames)
            .containsExactly(
                tenantAName
            );
    }


    @Test
    void shouldRejectCrossTenantCategoryInsert() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "rls-write-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "rls-write-b-" + suffix
            );


        assertThatThrownBy(
            () ->
                tenantContextExecutor.execute(
                    tenantA,
                    () ->
                        jdbcTemplate.update(
                            """
                            INSERT INTO categories (
                                tenant_id,
                                name,
                                description,
                                is_active
                            )
                            VALUES (
                                ?,
                                ?,
                                'Cross tenant test',
                                TRUE
                            )
                            """,
                            tenantB,
                            "Forbidden Category " +
                            suffix
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
            "RLS Test Tenant",
            slug
        );
    }


    private void createCategory(
            Integer tenantId,
            String name
    ) {

        tenantContextExecutor.execute(
            tenantId,
            () ->
                jdbcTemplate.update(
                    """
                    INSERT INTO categories (
                        tenant_id,
                        name,
                        description,
                        is_active
                    )
                    VALUES (
                        ?,
                        ?,
                        'RLS test category',
                        TRUE
                    )
                    """,
                    tenantId,
                    name
                )
        );
    }
}