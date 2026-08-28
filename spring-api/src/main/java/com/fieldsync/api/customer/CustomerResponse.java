package com.fieldsync.api.customer;

import com.fieldsync.api.domain.entity.CustomerEntity;

import java.time.LocalDateTime;


public record CustomerResponse(

    Integer id,
    Integer tenant_id,

    String name,
    String phone,
    String email,
    String address,

    Boolean is_active,

    LocalDateTime created_at,
    LocalDateTime updated_at

) {

    public static CustomerResponse from(
            CustomerEntity customer
    ) {

        return new CustomerResponse(
            customer.getId(),
            customer.getTenant().getId(),

            customer.getName(),
            customer.getPhone(),
            customer.getEmail(),
            customer.getAddress(),

            customer.getActive(),

            customer.getCreatedAt(),
            customer.getUpdatedAt()
        );
    }
}