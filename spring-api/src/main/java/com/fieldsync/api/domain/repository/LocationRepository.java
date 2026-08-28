package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.LocationEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface LocationRepository
        extends JpaRepository<LocationEntity, Integer> {

    List<LocationEntity>
    findAllByTenant_IdAndActiveTrueOrderByIdDesc(
        Integer tenantId
    );

    Optional<LocationEntity>
    findByIdAndTenant_Id(
        Integer id,
        Integer tenantId
    );
}