package com.freightos.fms.adapter.out.persistence.freight;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

/**
 * bms.financial_document 읽기 전용 참조 엔티티.
 * FMS 조회 시 financial_document_id → document_no 변환에만 사용.
 * bms 스키마 cross-schema 접근은 freight 패키지 내에만 격리된다.
 */
@Entity
@Immutable
@Table(schema = "bms", name = "financial_document")
@Getter
@NoArgsConstructor
public class FinancialDocumentRefJpaEntity {

    @Id
    @Column(name = "financial_document_id", updatable = false, nullable = false)
    private Long financialDocumentId;

    @Column(name = "document_no", nullable = false, length = 20)
    private String documentNo;
}
