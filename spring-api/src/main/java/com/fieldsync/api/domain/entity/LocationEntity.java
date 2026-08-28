package com.fieldsync.api.domain.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "locations")
@SequenceGenerator(
    name = "locations_seq",
    sequenceName = "locations_id_seq",
    allocationSize = 1
)
public class LocationEntity {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "locations_seq"
    )
    private Integer id;

    @Column(nullable = false, length = 150)
    private String name;

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


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
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


    public static LocationEntity create(
            TenantEntity tenant,
            String name,
            String address
    ) {

        LocationEntity location =
            new LocationEntity();

        LocalDateTime now =
            LocalDateTime.now();

        location.tenant = tenant;
        location.name = name;
        location.address = address;
        location.active = true;
        location.createdAt = now;
        location.updatedAt = now;

        return location;
    }


    public void update(
            String name,
            String address
    ) {

        this.name = name;
        this.address = address;
        this.updatedAt =
            LocalDateTime.now();
    }


    protected LocationEntity() {
    }
}