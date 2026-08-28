package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.CapturedImageEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;


public interface CapturedImageRepository
        extends JpaRepository<CapturedImageEntity, Integer> {


    List<CapturedImageEntity>
    findAllByCapturedRecord_IdAndTenant_IdOrderByIdAsc(
        Integer capturedRecordId,
        Integer tenantId
    );


    List<CapturedImageEntity>
    findAllByCapturedRecord_IdInAndTenant_IdOrderByIdAsc(
        Collection<Integer> capturedRecordIds,
        Integer tenantId
    );
}