package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.CapturedRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CapturedRecordRepository
        extends JpaRepository<CapturedRecordEntity, Integer> {

    List<CapturedRecordEntity> findAllByTenant_Id(
        Integer tenantId
    );

    Optional<CapturedRecordEntity> findByIdAndTenant_Id(
        Integer id,
        Integer tenantId
    );
}