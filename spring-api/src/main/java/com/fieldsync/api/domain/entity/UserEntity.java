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

    protected UserEntity() {
    }
}