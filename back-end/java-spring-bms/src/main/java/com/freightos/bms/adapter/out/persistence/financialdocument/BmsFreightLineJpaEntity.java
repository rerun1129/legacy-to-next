package com.freightos.bms.adapter.out.persistence.financialdocument;

import com.freightos.common.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * BMS 전용 freight_line 읽기/부분수정 엔티티.
 * FMS의 FreightLineJpaEntity와 같은 테이블을 매핑하지만 별개 클래스.
 * 연관관계(@ManyToOne) 없이 freight_header_id·financial_document_id는 Long 컬럼으로 직접 매핑.
 * financial_document_id·performance_dt는 mutable(서류 발행·연결 시 갱신).
 * 나머지 금액·코드 컬럼은 읽기용(setter 미제공).
 */
@Entity
@Table(schema = "bms", name = "freight_line")
@Getter
@NoArgsConstructor
public class BmsFreightLineJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freight_line_id", updatable = false, nullable = false)
    private Long freightLineId;

    @Column(name = "freight_header_id", nullable = false, updatable = false)
    private Long freightHeaderId;

    @Column(name = "freight_type", nullable = false, length = 10)
    private String freightType;

    @Column(name = "financial_doc_type", length = 10)
    private String financialDocType;

    @Column(name = "freight_code", length = 40)
    private String freightCode;

    @Column(name = "unit_quantity", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal unitQuantity;

    @Column(name = "unit_price", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal unitPrice;

    @Column(name = "per", length = 40)
    private String per;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "exchange_rate", columnDefinition = "NUMERIC(18,6)")
    private BigDecimal exchangeRate;

    @Column(name = "settle_amount", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal settleAmount;

    @Column(name = "local_amount", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal localAmount;

    @Column(name = "settle_tax_amount", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal settleTaxAmount;

    @Column(name = "local_tax_amount", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal localTaxAmount;

    @Column(name = "usd_exchange_rate", columnDefinition = "NUMERIC(18,6)")
    private BigDecimal usdExchangeRate;

    @Column(name = "usd_amount", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal usdAmount;

    @Column(name = "customer_code", length = 40)
    private String customerCode;

    @Column(name = "tax_type", length = 12)
    private String taxType;

    @Column(name = "tax_no", length = 50)
    private String taxNo;

    @Column(name = "tax_dt", length = 8)
    private String taxDt;

    @Column(name = "slip_no", length = 50)
    private String slipNo;

    @Column(name = "slip_dt", length = 8)
    private String slipDt;

    // ── 서류 연결 시 갱신되는 mutable 필드 ────────────────────────────────────

    @Column(name = "financial_document_id")
    private Long financialDocumentId;

    @Column(name = "performance_dt", length = 8)
    private String performanceDt;

    // ── Mutable setters (서류 연결·해제 전용) ─────────────────────────────────

    public void setFinancialDocumentId(Long v) { this.financialDocumentId = v; }
    public void setPerformanceDt(String v) { this.performanceDt = v; }
}
