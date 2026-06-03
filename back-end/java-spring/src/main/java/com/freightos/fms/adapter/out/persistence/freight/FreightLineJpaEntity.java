package com.freightos.fms.adapter.out.persistence.freight;

import com.freightos.common.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * JPA ORM 엔티티 — bms.freight_line.
 * FreightHeaderJpaEntity에 @ManyToOne FK(updatable=false).
 */
@Entity
@Table(schema = "bms", name = "freight_line")
@Getter
@NoArgsConstructor
public class FreightLineJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freight_line_id", updatable = false, nullable = false)
    private Long freightLineId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "freight_header_id", nullable = false, updatable = false)
    private FreightHeaderJpaEntity freightHeader;

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

    /** Per 기준 — Per 코드 또는 컨테이너 타입 코드 혼재(§6.6). VARCHAR(40) String 저장. */
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

    @Column(name = "performance_dt", length = 8)
    private String performanceDt;

    // ── 단계B 진입 시 채워질 컬럼 (A단계: null 저장) ─────────────────────────
    @Column(name = "tax_no", length = 50)
    private String taxNo;

    @Column(name = "tax_dt", length = 8)
    private String taxDt;

    @Column(name = "slip_no", length = 50)
    private String slipNo;

    @Column(name = "slip_dt", length = 8)
    private String slipDt;

    @Column(name = "financial_document_id")
    private Long financialDocumentId;

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setFreightHeader(FreightHeaderJpaEntity v) { this.freightHeader = v; }
    public void setFreightType(String v) { this.freightType = v; }
    public void setFinancialDocType(String v) { this.financialDocType = v; }
    public void setFreightCode(String v) { this.freightCode = v; }
    public void setUnitQuantity(BigDecimal v) { this.unitQuantity = v; }
    public void setUnitPrice(BigDecimal v) { this.unitPrice = v; }
    public void setPer(String v) { this.per = v; }
    public void setCurrency(String v) { this.currency = v; }
    public void setExchangeRate(BigDecimal v) { this.exchangeRate = v; }
    public void setSettleAmount(BigDecimal v) { this.settleAmount = v; }
    public void setLocalAmount(BigDecimal v) { this.localAmount = v; }
    public void setSettleTaxAmount(BigDecimal v) { this.settleTaxAmount = v; }
    public void setLocalTaxAmount(BigDecimal v) { this.localTaxAmount = v; }
    public void setUsdExchangeRate(BigDecimal v) { this.usdExchangeRate = v; }
    public void setUsdAmount(BigDecimal v) { this.usdAmount = v; }
    public void setCustomerCode(String v) { this.customerCode = v; }
    public void setTaxType(String v) { this.taxType = v; }
    public void setPerformanceDt(String v) { this.performanceDt = v; }
    public void setTaxNo(String v) { this.taxNo = v; }
    public void setTaxDt(String v) { this.taxDt = v; }
    public void setSlipNo(String v) { this.slipNo = v; }
    public void setSlipDt(String v) { this.slipDt = v; }
    public void setFinancialDocumentId(Long v) { this.financialDocumentId = v; }
}
