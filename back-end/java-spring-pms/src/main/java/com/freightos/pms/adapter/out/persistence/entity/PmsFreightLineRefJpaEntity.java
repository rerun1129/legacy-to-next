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
 * bms.freight_line 읽기 전용 참조 엔티티.
 * PMS 집계 쿼리에서 운임행 합산 소스로만 사용된다.
 */
@Entity
@Immutable
@Table(schema = "bms", name = "freight_line")
@Getter
@NoArgsConstructor
public class PmsFreightLineRefJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freight_line_id")
    private Long freightLineId;

    @Column(name = "freight_header_id")
    private Long freightHeaderId;

    @Column(name = "financial_doc_type", length = 20)
    private String financialDocType;

    @Column(name = "local_amount", precision = 20, scale = 4)
    private BigDecimal localAmount;

    @Column(name = "usd_amount", precision = 20, scale = 4)
    private BigDecimal usdAmount;

    @Column(name = "tax_no", length = 50)
    private String taxNo;

    @Column(name = "slip_no", length = 50)
    private String slipNo;

    @Column(name = "tax_type", length = 20)
    private String taxType;

    @Column(name = "performance_dt", length = 8)
    private String performanceDt;

    @Column(name = "financial_document_id")
    private Long financialDocumentId;

    @Column(name = "customer_code", length = 40)
    private String customerCode;

    @Column(name = "freight_type", length = 20)
    private String freightType;
}
