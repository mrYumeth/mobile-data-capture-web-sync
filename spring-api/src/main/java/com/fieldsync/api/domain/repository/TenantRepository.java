package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.TenantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository
        extends JpaRepository<TenantEntity, Integer> {

    Optional<TenantEntity> findBySlug(String slug);

    Optional<TenantEntity> findByIdAndActiveTrue(Integer id);
}