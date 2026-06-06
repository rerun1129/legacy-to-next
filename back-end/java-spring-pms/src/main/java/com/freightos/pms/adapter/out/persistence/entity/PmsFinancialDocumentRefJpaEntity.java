package com.freightos.pms.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * bms.financial_document 읽기 전용 참조 엔티티.
 * PMS DOCUMENT_CREATED 기준 집계 쿼리에서만 사용된다.
 */
@Entity
@Immutable
@Table(schema = "bms", name = "financial_document")
@Getter
@NoArgsConstructor
public class PmsFinancialDocumentRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "financial_document_id")
    private Long financialDocumentId;

    @Column(name = "document_type", length = 20)
    private String documentType;

    @Column(name = "document_status", length = 20)
    private String documentStatus;

    @Column(name = "document_no", length = 50)
    private String documentNo;

    @Column(name = "document_dt", length = 8)
    private String documentDt;

    @Column(name = "group_financial_no", length = 50)
    private String groupFinancialNo;

    @Column(name = "local_total_amount", precision = 20, scale = 4)
    private BigDecimal localTotalAmount;

    @Column(name = "usd_total_amount", precision = 20, scale = 4)
    private BigDecimal usdTotalAmount;

    @Column(name = "team_code", length = 20)
    private String teamCode;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "performance_dt", length = 8)
    private String performanceDt;

    @Column(name = "customer_code", length = 40)
    private String customerCode;
}
