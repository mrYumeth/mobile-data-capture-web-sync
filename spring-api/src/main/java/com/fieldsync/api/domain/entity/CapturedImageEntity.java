package com.fieldsync.api.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "captured_images")
@SequenceGenerator(
    name = "captured_images_seq",
    sequenceName = "captured_images_id_seq",
    allocationSize = 1
)
public class CapturedImageEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "captured_images_seq"
    )
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captured_record_id")
    private CapturedRecordEntity capturedRecord;

    @Column(
        name = "image_url",
        columnDefinition = "text"
    )
    private String imageUrl;

    @Column(
        name = "storage_path",
        columnDefinition = "text"
    )
    private String storagePath;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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

    public CapturedRecordEntity getCapturedRecord() {
        return capturedRecord;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public TenantEntity getTenant() {
        return tenant;
    }

    protected CapturedImageEntity() {
    }
}