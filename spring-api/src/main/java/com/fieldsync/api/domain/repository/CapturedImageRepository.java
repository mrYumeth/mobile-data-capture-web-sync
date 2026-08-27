package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.CapturedImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CapturedImageRepository
        extends JpaRepository<CapturedImageEntity, Integer> {

    List<CapturedImageEntity>
    findAllByCapturedRecord_IdAndTenant_Id(
        Integer capturedRecordId,
        Integer tenantId
    );
}