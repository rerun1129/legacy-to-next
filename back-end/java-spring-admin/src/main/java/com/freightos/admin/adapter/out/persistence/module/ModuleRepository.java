package com.freightos.admin.adapter.out.persistence.module;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModuleRepository extends JpaRepository<ModuleJpaEntity, Long>, ModuleRepositoryCustom {
    Optional<ModuleJpaEntity> findByModuleCode(String moduleCode);
    boolean existsByModuleCode(String moduleCode);
}
