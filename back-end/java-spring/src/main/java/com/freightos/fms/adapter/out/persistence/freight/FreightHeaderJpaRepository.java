package com.freightos.fms.adapter.out.persistence.freight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * bms.freight_header Spring Data JPA 리포지토리.
 * bl_type+bl_id UNIQUE 조합으로 헤더 조회.
 */
public interface FreightHeaderJpaRepository extends JpaRepository<FreightHeaderJpaEntity, Long> {

    Optional<FreightHeaderJpaEntity> findByBlTypeAndBlId(String blType, String blId);

    boolean existsByBlTypeAndBlId(String blType, String blId);
}
