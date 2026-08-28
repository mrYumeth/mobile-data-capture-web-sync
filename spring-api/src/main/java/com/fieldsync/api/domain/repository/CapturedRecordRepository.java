package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.CapturedRecordEntity;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface CapturedRecordRepository
        extends JpaRepository<CapturedRecordEntity, Integer> {


    @EntityGraph(
        attributePaths = {
            "customer",
            "location",
            "category"
        }
    )
    List<CapturedRecordEntity>
    findAllByTenant_IdOrderByReceivedAtDesc(
        Integer tenantId
    );


    @EntityGraph(
        attributePaths = {
            "customer",
            "location",
            "category"
        }
    )
    Optional<CapturedRecordEntity>
    findByIdAndTenant_Id(
        Integer id,
        Integer tenantId
    );
}