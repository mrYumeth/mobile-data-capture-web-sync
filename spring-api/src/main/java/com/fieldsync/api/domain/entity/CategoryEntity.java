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

    protected CategoryEntity() {
    }
}