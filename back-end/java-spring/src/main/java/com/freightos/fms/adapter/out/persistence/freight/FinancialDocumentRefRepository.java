package com.freightos.fms.adapter.out.persistence.freight;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * bms.financial_document 읽기 전용 Spring Data JPA 리포지토리.
 * financial_document_id → document_no 일괄 조회 전용.
 */
public interface FinancialDocumentRefRepository extends JpaRepository<FinancialDocumentRefJpaEntity, Long> {

    List<FinancialDocumentRefJpaEntity> findByFinancialDocumentIdIn(Collection<Long> ids);
}
