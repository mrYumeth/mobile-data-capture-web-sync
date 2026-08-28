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


    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
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


    public static CustomerEntity create(
            TenantEntity tenant,
            String name,
            String phone,
            String email,
            String address
    ) {

        CustomerEntity customer =
            new CustomerEntity();

        LocalDateTime now =
            LocalDateTime.now();

        customer.tenant = tenant;
        customer.name = name;
        customer.phone = phone;
        customer.email = email;
        customer.address = address;
        customer.active = true;
        customer.createdAt = now;
        customer.updatedAt = now;

        return customer;
    }


    public void update(
            String name,
            String phone,
            String email,
            String address
    ) {

        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.updatedAt =
            LocalDateTime.now();
    }


    protected CustomerEntity() {
    }
}