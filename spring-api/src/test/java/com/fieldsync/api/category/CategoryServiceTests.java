package com.fieldsync.api.category;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.core.context
    .SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fieldsync.api.tenant.TenantContextExecutor;

@SpringBootTest
@Transactional
class CategoryServiceTests {

    @Autowired
    private CategoryService categoryService;

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
    void shouldReturnOnlyActiveCategoriesForAuthenticatedTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "tenant-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "tenant-b-" + suffix
            );

        String keycloakUserId =
            UUID.randomUUID()
                .toString();

        String email =
            "tenant-a-" +
            suffix +
            "@example.test";

        createUser(
            tenantA,
            "user-" + suffix,
            email,
            keycloakUserId,
            "user",
            true
        );

        createCategory(
            tenantA,
            "Tenant A First",
            true
        );

        createCategory(
            tenantA,
            "Tenant A Second",
            true
        );

        createCategory(
            tenantA,
            "Tenant A Inactive",
            false
        );

        createCategory(
            tenantB,
            "Tenant B Category",
            true
        );

        authenticate(
            keycloakUserId,
            email
        );

        List<CategoryResponse> categories =
            categoryService
                .getActiveCategories();

        assertThat(categories)
            .extracting(
                CategoryResponse::name
            )
            .containsExactly(
                "Tenant A Second",
                "Tenant A First"
            );

        assertThat(categories)
            .allMatch(
                category ->
                    category
                        .tenant_id()
                        .equals(tenantA)
            );
    }

    @Test
void shouldCreateCategoryForAuthenticatedTenant() {

    TestContext context =
        createAuthenticatedUser(
            "user",
            true
        );

    CategoryResponse created =
        categoryService.createCategory(
            new CategoryRequest(
                "  New Category  ",
                "Description"
            )
        );

    assertThat(created.name())
        .isEqualTo("New Category");

    assertThat(created.tenant_id())
        .isEqualTo(context.tenantId());

    assertThat(created.is_active())
        .isTrue();
}


@Test
void shouldUpdateOnlyCategoryFromAuthenticatedTenant() {

    TestContext context =
        createAuthenticatedUser(
            "user",
            true
        );

    Integer categoryId =
        createCategoryAndReturnId(
            context.tenantId(),
            "Old Category"
        );

    CategoryResponse updated =
        categoryService.updateCategory(
            categoryId,
            new CategoryRequest(
                "Updated Category",
                "Updated description"
            )
        );

    assertThat(updated.name())
        .isEqualTo(
            "Updated Category"
        );
}


@Test
void shouldAllowAdminToDeleteCategory() {

    TestContext context =
        createAuthenticatedUser(
            "admin",
            true
        );

    Integer categoryId =
        createCategoryAndReturnId(
            context.tenantId(),
            "Delete Category"
        );

    CategoryDeleteResponse response =
        categoryService.deleteCategory(
            categoryId
        );

    assertThat(response.message())
        .isEqualTo(
            "Category deleted successfully"
        );
}


@Test
void shouldRejectDeleteForNonAdminUser() {

    TestContext context =
        createAuthenticatedUser(
            "user",
            true
        );

    Integer categoryId =
        createCategoryAndReturnId(
            context.tenantId(),
            "Protected Category"
        );

    assertThatThrownBy(
        () ->
            categoryService
                .deleteCategory(
                    categoryId
                )
    )
    .isInstanceOf(
        CategoryApiException.class
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
            "crud-" + suffix
        );

    String keycloakUserId =
        UUID.randomUUID()
            .toString();

    String email =
        "crud-" +
        suffix +
        "@example.test";

    createUser(
        tenantId,
        "crud-user-" + suffix,
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

private Integer createCategoryAndReturnId(
        Integer tenantId,
        String name
) {

    return tenantContextExecutor.execute(
        tenantId,
        () ->
            jdbcTemplate.queryForObject(
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
                    'CRUD test category',
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
            'Test User',
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


        private void createCategory(
            Integer tenantId,
            String name,
            boolean active
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
                        'Test category',
                        ?
                    )
                    """,

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

                // Must never control tenant isolation.
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

