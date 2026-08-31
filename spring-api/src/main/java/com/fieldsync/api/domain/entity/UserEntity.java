package com.fieldsync.api.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@SequenceGenerator(
    name = "users_seq",
    sequenceName = "users_id_seq",
    allocationSize = 1
)
public class UserEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "users_seq"
    )
    private Integer id;

    @Column(
        nullable = false,
        unique = true,
        length = 100
    )
    private String username;

    @Column(
        name = "password_hash",
        nullable = false,
        columnDefinition = "text"
    )
    private String passwordHash;

    @Column(name = "full_name", length = 150)
    private String fullName;

    @Column(nullable = false, length = 50)
    private String role;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(length = 150)
    private String email;

    @Column(name = "access_web", nullable = false)
    private Boolean accessWeb;

    @Column(name = "access_mobile", nullable = false)
    private Boolean accessMobile;

    @Column(
        name = "password_change_required",
        nullable = false
    )
    private Boolean passwordChangeRequired;

    @Column(
        name = "confirmation_token",
        columnDefinition = "text"
    )
    private String confirmationToken;

    @Column(name = "confirmation_expires_at")
    private LocalDateTime confirmationExpiresAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserEntity createdBy;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false
    )
    private TenantEntity tenant;

    @Column(name = "keycloak_user_id")
    private String keycloakUserId;

    public Integer getId() {
    return id;
}

public String getUsername() {
    return username;
}

public String getEmail() {
    return email;
}

public String getFullName() {
    return fullName;
}

public String getRole() {
    return role;
}

public Boolean getActive() {
    return active;
}

public Boolean getAccessWeb() {
    return accessWeb;
}

public Boolean getAccessMobile() {
    return accessMobile;
}

public Boolean getPasswordChangeRequired() {
    return passwordChangeRequired;
}

public TenantEntity getTenant() {
    return tenant;
}

public String getKeycloakUserId() {
    return keycloakUserId;
}

public void linkKeycloakUser(
        String keycloakUserId
) {

    if (
        keycloakUserId == null ||
        keycloakUserId.isBlank()
    ) {
        throw new IllegalArgumentException(
            "Keycloak user ID is required"
        );
    }

    this.keycloakUserId = keycloakUserId;
    this.updatedAt = LocalDateTime.now();
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}


public LocalDateTime getConfirmedAt() {
    return confirmedAt;
}

public static UserEntity createKeycloakUser(
        TenantEntity tenant,
        UserEntity createdBy,
        String username,
        String email,
        String fullName,
        boolean accessWeb,
        boolean accessMobile,
        String keycloakUserId
) {

    if (tenant == null) {
        throw new IllegalArgumentException(
            "Tenant is required"
        );
    }

    if (
        keycloakUserId == null ||
        keycloakUserId.isBlank()
    ) {
        throw new IllegalArgumentException(
            "Keycloak user ID is required"
        );
    }


    LocalDateTime now =
        LocalDateTime.now();


    UserEntity user =
        new UserEntity();

    user.tenant =
        tenant;

    user.createdBy =
        createdBy;

    user.username =
        username;

    user.email =
        email;

    user.passwordHash =
        "KEYCLOAK_AUTH_ONLY";

    user.fullName =
        fullName;

    user.role =
        "user";

    user.active =
        true;

    user.accessWeb =
        accessWeb;

    user.accessMobile =
        accessMobile;

    user.passwordChangeRequired =
        false;

    user.confirmedAt =
        now;

    user.keycloakUserId =
        keycloakUserId;

    user.createdAt =
        now;

    user.updatedAt =
        now;


    return user;
}

public void updateAdminManagedUser(
        String fullName,
        String email,
        Boolean accessWeb,
        Boolean accessMobile,
        Boolean active
) {

    if (fullName != null) {
        this.fullName =
            fullName;
    }

    if (email != null) {
        this.email =
            email;
    }

    if (accessWeb != null) {
        this.accessWeb =
            accessWeb;
    }

    if (accessMobile != null) {
        this.accessMobile =
            accessMobile;
    }

    if (active != null) {
        this.active =
            active;
    }

    this.updatedAt =
        LocalDateTime.now();
}

public static UserEntity createKeycloakAdmin(
        TenantEntity tenant,
        String username,
        String email,
        String fullName,
        String keycloakUserId
) {

    if (tenant == null) {

        throw new IllegalArgumentException(
            "Tenant is required"
        );
    }


    if (
        keycloakUserId == null ||
        keycloakUserId.isBlank()
    ) {

        throw new IllegalArgumentException(
            "Keycloak user ID is required"
        );
    }


    LocalDateTime now =
        LocalDateTime.now();


    UserEntity user =
        new UserEntity();

    user.tenant =
        tenant;

    user.createdBy =
        null;

    user.username =
        username;

    user.email =
        email;

    user.passwordHash =
        "KEYCLOAK_AUTH_ONLY";

    user.fullName =
        fullName;

    user.role =
        "admin";

    user.active =
        true;

    user.accessWeb =
        true;

    user.accessMobile =
        true;

    user.passwordChangeRequired =
        false;

    user.confirmedAt =
        now;

    user.keycloakUserId =
        keycloakUserId;

    user.createdAt =
        now;

    user.updatedAt =
        now;


    return user;
}

    protected UserEntity() {
    }
}