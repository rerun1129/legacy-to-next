package com.freightos.bms.adapter.out.persistence.financialdocument;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * freight_header 읽기 전용 JpaRepository.
 * blType+blId 기반 headerId 조회 전용.
 */
public interface FreightHeaderRefRepository extends JpaRepository<FreightHeaderRefJpaEntity, Long> {

    Optional<FreightHeaderRefJpaEntity> findByBlTypeAndBlId(String blType, String blId);
}
