package com.fieldsync.api.domain.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "captured_records")
@SequenceGenerator(
    name = "captured_records_seq",
    sequenceName = "captured_records_id_seq",
    allocationSize = 1
)
public class CapturedRecordEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "captured_records_seq"
    )
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private LocationEntity location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Column(columnDefinition = "text")
    private String description;

    @Column(
        precision = 10,
        scale = 7
    )
    private BigDecimal latitude;

    @Column(
        precision = 10,
        scale = 7
    )
    private BigDecimal longitude;

    @Column(
        name = "image_url",
        columnDefinition = "text"
    )
    private String imageUrl;

    @Column(
        name = "image_path",
        columnDefinition = "text"
    )
    private String imagePath;

    @Column(name = "captured_at")
    private LocalDateTime capturedAt;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

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

    protected CapturedRecordEntity() {
    }
}