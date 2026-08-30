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

    public Integer getId() {
    return id;
}

    public CustomerEntity getCustomer() {
        return customer;
    }

    public LocationEntity getLocation() {
        return location;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public LocalDateTime getCapturedAt() {
        return capturedAt;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
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

    public static CapturedRecordEntity create(
        TenantEntity tenant,
        CustomerEntity customer,
        LocationEntity location,
        CategoryEntity category,
        String description,
        BigDecimal latitude,
        BigDecimal longitude,
        String imageUrl,
        String imagePath,
        LocalDateTime capturedAt
) {

    CapturedRecordEntity record =
        new CapturedRecordEntity();

    LocalDateTime now =
        LocalDateTime.now();

    record.tenant = tenant;

    record.customer = customer;
    record.location = location;
    record.category = category;

    record.description = description;

    record.latitude = latitude;
    record.longitude = longitude;

    record.imageUrl = imageUrl;
    record.imagePath = imagePath;

    record.capturedAt = capturedAt;

    record.receivedAt = now;
    record.createdAt = now;
    record.updatedAt = now;

    return record;
}

    protected CapturedRecordEntity() {
    }
}