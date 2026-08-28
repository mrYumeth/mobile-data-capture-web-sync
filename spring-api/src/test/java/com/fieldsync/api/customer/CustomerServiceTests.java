package com.fieldsync.api.customer;

import com.fieldsync.api.tenant.TenantContextExecutor;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.core.context
    .SecurityContextHolder;

import org.springframework.security.oauth2.jwt.Jwt;

import org.springframework.security.oauth2.server.resource.authentication
    .JwtAuthenticationToken;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
@Transactional
class CustomerServiceTests {

    @Autowired
    private CustomerService customerService;

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
    void shouldReturnOnlyCustomersForAuthenticatedTenant() {

        String suffix =
            UUID.randomUUID()
                .toString()
                .replace("-", "");

        Integer tenantA =
            createTenant(
                "customer-a-" + suffix
            );

        Integer tenantB =
            createTenant(
                "customer-b-" + suffix
            );

        String keycloakUserId =
            UUID.randomUUID()
                .toString();

        String email =
            "customer-" +
            suffix +
            "@example.test";

        createUser(
            tenantA,
            "customer-user-" + suffix,
            email,
            keycloakUserId,
            "user",
            true
        );

        createCustomerAndReturnId(
            tenantA,
            "Tenant A First"
        );

        createCustomerAndReturnId(
            tenantA,
            "Tenant A Second"
        );

        createCustomerAndReturnId(
            tenantB,
            "Tenant B Customer"
        );

        authenticate(
            keycloakUserId,
            email
        );

        List<CustomerResponse> customers =
            customerService
                .getCustomers();

        assertThat(customers)
            .extracting(
                CustomerResponse::name
            )
            .containsExactly(
                "Tenant A Second",
                "Tenant A First"
            );

        assertThat(customers)
            .allMatch(
                customer ->
                    customer
                        .tenant_id()
                        .equals(tenantA)
            );
    }


    @Test
    void shouldCreateCustomerForAuthenticatedTenant() {

        TestContext context =
            createAuthenticatedUser(
                "user",
                true
            );

        CustomerResponse created =
            customerService.createCustomer(
                new CustomerRequest(
                    "  New Customer  ",
                    "0712345678",
                    "customer@example.test",
                    "Colombo"
                )
            );

        assertThat(created.name())
            .isEqualTo(
                "New Customer"
            );

        assertThat(created.phone())
            .isEqualTo(
                "0712345678"
            );

        assertThat(created.tenant_id())
            .isEqualTo(
                context.tenantId()
            );

        assertThat(created.is_active())
            .isTrue();
    }


    @Test
    void shouldRejectInvalidPhoneNumber() {

        createAuthenticatedUser(
            "user",
            true
        );

        assertThatThrownBy(
            () ->
                customerService.createCustomer(
                    new CustomerRequest(
                        "Invalid Phone Customer",
                        "12345",
                        null,
                        null
                    )
                )
        )
        .isInstanceOf(
            CustomerApiException.class
        )
        .hasMessage(
            "Phone number must contain exactly 10 digits"
        );
    }


    @Test
    void shouldUpdateCustomer() {

        TestContext context =
            createAuthenticatedUser(
                "user",
                true
            );

        Integer customerId =
            createCustomerAndReturnId(
                context.tenantId(),
                "Old Customer"
            );

        CustomerResponse updated =
            customerService.updateCustomer(
                customerId,
                new CustomerRequest(
                    "Updated Customer",
                    "0771234567",
                    "updated@example.test",
                    "Kandy"
                )
            );

        assertThat(updated.name())
            .isEqualTo(
                "Updated Customer"
            );

        assertThat(updated.phone())
            .isEqualTo(
                "0771234567"
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
                customerService.createCustomer(
                    new CustomerRequest(
                        "Forbidden Customer",
                        null,
                        null,
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
    void shouldAllowAdminToDeleteCustomer() {

        TestContext context =
            createAuthenticatedUser(
                "admin",
                true
            );

        Integer customerId =
            createCustomerAndReturnId(
                context.tenantId(),
                "Delete Customer"
            );

        CustomerDeleteResponse response =
            customerService.deleteCustomer(
                customerId
            );

        assertThat(response.message())
            .isEqualTo(
                "Customer deleted successfully"
            );

        assertThat(
            response.deletedCustomer().id()
        )
        .isEqualTo(
            customerId
        );
    }


    @Test
    void shouldRejectDeleteForNonAdminUser() {

        TestContext context =
            createAuthenticatedUser(
                "user",
                true
            );

        Integer customerId =
            createCustomerAndReturnId(
                context.tenantId(),
                "Protected Customer"
            );

        assertThatThrownBy(
            () ->
                customerService
                    .deleteCustomer(
                        customerId
                    )
        )
        .isInstanceOf(
            CustomerApiException.class
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
                "customer-crud-" + suffix
            );

        String keycloakUserId =
            UUID.randomUUID()
                .toString();

        String email =
            "customer-crud-" +
            suffix +
            "@example.test";

        createUser(
            tenantId,
            "customer-crud-user-" + suffix,
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
            "Customer Test Tenant",
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
                'Customer Test User',
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


    private Integer createCustomerAndReturnId(
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
                        phone,
                        email,
                        address,
                        is_active
                    )
                    VALUES (
                        ?,
                        ?,
                        NULL,
                        NULL,
                        NULL,
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
                // Tenant must come from the database.
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