package com.freightos.bms.adapter.out.persistence.financialdocument;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * financial_document 테이블 CRUD JpaRepository.
 */
public interface FinancialDocumentRepository extends JpaRepository<FinancialDocumentJpaEntity, Long> {
}
