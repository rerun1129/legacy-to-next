package com.freightos.fms.domain.freight;

import com.freightos.fms.domain.freight.enums.FinancialDocType;
import com.freightos.fms.domain.freight.enums.FreightType;
import com.freightos.fms.domain.freight.enums.TaxType;

import java.math.BigDecimal;

/**
 * 운임 라인 도메인 엔티티.
 * 입력값 + 계산값을 모두 보유한다.
 * 순수 Java — Spring/JPA import 없음.
 */
public class FreightLine {

    // ── 입력값 ──────────────────────────────────────────────────────────────────
    private FreightType freightType;
    private String freightCode;
    /** Per 기준 — Per enum 코드 또는 컨테이너 타입 코드 혼재(§6.6 CNTR 동적). VARCHAR(40) String 저장. */
    private String per;
    private BigDecimal unitQuantity;
    private BigDecimal unitPrice;
    private String currency;
    private String customerCode;
    private TaxType taxType;
    private String performanceDt;

    // ── 계산값 ──────────────────────────────────────────────────────────────────
    /** §6.16: (FreightType, customerType) → FinancialDocType 자동 산정 */
    private FinancialDocType financialDocType;
    private BigDecimal exchangeRate;
    private BigDecimal settleAmount;
    private BigDecimal localAmount;
    private BigDecimal settleTaxAmount;
    private BigDecimal localTaxAmount;
    private BigDecimal usdExchangeRate;
    private BigDecimal usdAmount;

    // ── 단계B 진입 시 채워질 필드 (저장 시 null) ────────────────────────────────
    private String taxNo;
    private String taxDt;
    private String slipNo;
    private String slipDt;
    private Long financialDocumentId;

    public FreightLine() {}

    // ── Getters ────────────────────────────────────────────────────────────────

    public FreightType getFreightType() { return freightType; }
    public String getFreightCode() { return freightCode; }
    public String getPer() { return per; }
    public BigDecimal getUnitQuantity() { return unitQuantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public String getCurrency() { return currency; }
    public String getCustomerCode() { return customerCode; }
    public TaxType getTaxType() { return taxType; }
    public String getPerformanceDt() { return performanceDt; }
    public FinancialDocType getFinancialDocType() { return financialDocType; }
    public BigDecimal getExchangeRate() { return exchangeRate; }
    public BigDecimal getSettleAmount() { return settleAmount; }
    public BigDecimal getLocalAmount() { return localAmount; }
    public BigDecimal getSettleTaxAmount() { return settleTaxAmount; }
    public BigDecimal getLocalTaxAmount() { return localTaxAmount; }
    public BigDecimal getUsdExchangeRate() { return usdExchangeRate; }
    public BigDecimal getUsdAmount() { return usdAmount; }
    public String getTaxNo() { return taxNo; }
    public String getTaxDt() { return taxDt; }
    public String getSlipNo() { return slipNo; }
    public String getSlipDt() { return slipDt; }
    public Long getFinancialDocumentId() { return financialDocumentId; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setFreightType(FreightType v) { this.freightType = v; }
    public void setFreightCode(String v) { this.freightCode = v; }
    public void setPer(String v) { this.per = v; }
    public void setUnitQuantity(BigDecimal v) { this.unitQuantity = v; }
    public void setUnitPrice(BigDecimal v) { this.unitPrice = v; }
    public void setCurrency(String v) { this.currency = v; }
    public void setCustomerCode(String v) { this.customerCode = v; }
    public void setTaxType(TaxType v) { this.taxType = v; }
    public void setPerformanceDt(String v) { this.performanceDt = v; }
    public void setFinancialDocType(FinancialDocType v) { this.financialDocType = v; }
    public void setExchangeRate(BigDecimal v) { this.exchangeRate = v; }
    public void setSettleAmount(BigDecimal v) { this.settleAmount = v; }
    public void setLocalAmount(BigDecimal v) { this.localAmount = v; }
    public void setSettleTaxAmount(BigDecimal v) { this.settleTaxAmount = v; }
    public void setLocalTaxAmount(BigDecimal v) { this.localTaxAmount = v; }
    public void setUsdExchangeRate(BigDecimal v) { this.usdExchangeRate = v; }
    public void setUsdAmount(BigDecimal v) { this.usdAmount = v; }
    public void setTaxNo(String v) { this.taxNo = v; }
    public void setTaxDt(String v) { this.taxDt = v; }
    public void setSlipNo(String v) { this.slipNo = v; }
    public void setSlipDt(String v) { this.slipDt = v; }
    public void setFinancialDocumentId(Long v) { this.financialDocumentId = v; }
}
