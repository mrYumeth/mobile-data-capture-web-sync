package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<CategoryEntity, Integer> {

    List<CategoryEntity> findAllByTenant_Id(
        Integer tenantId
    );

    Optional<CategoryEntity> findByIdAndTenant_Id(
        Integer id,
        Integer tenantId
    );

    List<CategoryEntity>
findAllByTenant_IdAndActiveTrueOrderByIdDesc(
    Integer tenantId
);
}