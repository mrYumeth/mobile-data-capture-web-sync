package com.fieldsync.api.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "tenants",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "tenants_slug_key",
            columnNames = "slug"
        )
    }
)
@SequenceGenerator(
    name = "tenants_seq",
    sequenceName = "tenants_id_seq",
    allocationSize = 1
)
public class TenantEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "tenants_seq"
    )
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String slug;

    @Column(name = "is_active", nullable = false)
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Integer getId() {
    return id;
}

public String getName() {
    return name;
}

public String getSlug() {
    return slug;
}

public Boolean getActive() {
    return active;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}


public static TenantEntity create(
        String name,
        String slug
) {

    LocalDateTime now =
        LocalDateTime.now();


    TenantEntity tenant =
        new TenantEntity();

    tenant.name =
        name;

    tenant.slug =
        slug;

    tenant.active =
        true;

    tenant.createdAt =
        now;

    tenant.updatedAt =
        now;


    return tenant;
}

    protected TenantEntity() {
    }
}