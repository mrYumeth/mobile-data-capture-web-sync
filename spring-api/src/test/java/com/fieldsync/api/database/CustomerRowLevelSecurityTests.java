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
class CustomerRowLevelSecurityTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TenantContextExecutor tenantContextExecutor;


    @Test
    void shouldOnlyExposeCustomersForCurrentDatabaseTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "customer-rls-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "customer-rls-b-" + suffix
            );

        String tenantAName =
            "Customer RLS A " + suffix;

        String tenantBName =
            "Customer RLS B " + suffix;


        createCustomer(
            tenantA,
            tenantAName
        );

        createCustomer(
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
                        FROM customers
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
    void shouldRejectCrossTenantCustomerInsert() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "customer-write-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "customer-write-b-" + suffix
            );


        assertThatThrownBy(
            () ->
                tenantContextExecutor.execute(
                    tenantA,
                    () ->
                        jdbcTemplate.update(
                            """
                            INSERT INTO customers (
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
                            "Forbidden Customer " +
                            suffix
                        )
                )
        )
        .isInstanceOf(
            DataAccessException.class
        );
    }


    @Test
    void shouldNotUpdateCustomerFromAnotherTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "customer-update-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "customer-update-b-" + suffix
            );

        Integer tenantBCustomer =
            createCustomer(
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
                        UPDATE customers
                        SET name = ?
                        WHERE id = ?
                        """,
                        "Illegal Update",
                        tenantBCustomer
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
            "Customer RLS Tenant",
            slug
        );
    }


    private Integer createCustomer(
            Integer tenantId,
            String name
    ) {

        return tenantContextExecutor.execute(
            tenantId,
            () ->
                jdbcTemplate.queryForObject(
                    """
                    INSERT INTO customers (
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