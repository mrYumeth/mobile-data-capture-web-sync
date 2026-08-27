package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<CustomerEntity, Integer> {

    List<CustomerEntity> findAllByTenant_Id(
        Integer tenantId
    );

    Optional<CustomerEntity> findByIdAndTenant_Id(
        Integer id,
        Integer tenantId
    );
}