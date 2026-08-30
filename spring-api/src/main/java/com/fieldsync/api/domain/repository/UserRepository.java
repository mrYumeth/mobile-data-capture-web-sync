package com.fieldsync.api.domain.repository;

import com.fieldsync.api.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByKeycloakUserId(
        String keycloakUserId
    );

    Optional<UserEntity> findByUsernameIgnoreCase(
        String username
    );

    Optional<UserEntity> findByEmailIgnoreCase(
        String email
    );

    Optional<UserEntity> findByIdAndTenant_Id(
    Integer id,
    Integer tenantId
);

    List<UserEntity> findAllByTenant_Id(
        Integer tenantId
    );

    List<UserEntity> findAllByTenant_IdOrderByCreatedAtDesc(
    Integer tenantId
    );
}