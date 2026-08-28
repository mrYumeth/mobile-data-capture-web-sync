package com.fieldsync.api.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "categories")
@SequenceGenerator(
    name = "categories_seq",
    sequenceName = "categories_id_seq",
    allocationSize = 1
)
public class CategoryEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "categories_seq"
    )
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(
        fetch = FetchType.LAZY,
        optional = false
    )
    @JoinColumn(
        name = "tenant_id",
        nullable = false
    )
    private TenantEntity tenant;

    public Integer getId() {
    return id;
}

public String getName() {
    return name;
}

public String getDescription() {
    return description;
}

public Boolean getActive() {
    return active;
}

public LocalDateTime getCreatedAt() {
    return createdAt;
}

public LocalDateTime getUpdatedAt() {
    return updatedAt;
}

public TenantEntity getTenant() {
    return tenant;
}

public static CategoryEntity create(
        TenantEntity tenant,
        String name,
        String description
) {

    CategoryEntity category =
        new CategoryEntity();

    LocalDateTime now =
        LocalDateTime.now();

    category.tenant = tenant;
    category.name = name;
    category.description = description;
    category.active = true;
    category.createdAt = now;
    category.updatedAt = now;

    return category;
}


public void update(
        String name,
        String description
) {

    this.name = name;
    this.description = description;
    this.updatedAt =
        LocalDateTime.now();
}

    protected CategoryEntity() {
    }
}