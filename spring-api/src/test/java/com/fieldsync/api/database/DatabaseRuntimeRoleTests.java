package com.fieldsync.api.database;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DatabaseRuntimeRoleTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.username}")
    private String expectedRuntimeUser;

    @Test
    void springShouldUseRestrictedRuntimeDatabaseRole() {

        Map<String, Object> role =
            jdbcTemplate.queryForMap(
                """
                SELECT
                    current_user AS role_name,
                    rolsuper AS superuser,
                    rolbypassrls AS bypass_rls
                FROM pg_roles
                WHERE rolname = current_user
                """
            );

        assertThat(
            role.get("role_name")
        ).isEqualTo(
            expectedRuntimeUser
        );

        assertThat(
            role.get("superuser")
        ).isEqualTo(false);

        assertThat(
            role.get("bypass_rls")
        ).isEqualTo(false);


        String categoryOwner =
            jdbcTemplate.queryForObject(
                """
                SELECT tableowner
                FROM pg_tables
                WHERE schemaname = 'public'
                  AND tablename = 'categories'
                """,
                String.class
            );

        assertThat(categoryOwner)
            .isNotEqualTo(
                expectedRuntimeUser
            );
    }
}