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

    protected UserEntity() {
    }
}