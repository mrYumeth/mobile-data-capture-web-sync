package com.fieldsync.api.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
@SequenceGenerator(
    name = "customers_seq",
    sequenceName = "customers_id_seq",
    allocationSize = 1
)
public class CustomerEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "customers_seq"
    )
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String phone;

    @Column(length = 150)
    private String email;

    @Column(columnDefinition = "text")
    private String address;

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

    protected CustomerEntity() {
    }
}