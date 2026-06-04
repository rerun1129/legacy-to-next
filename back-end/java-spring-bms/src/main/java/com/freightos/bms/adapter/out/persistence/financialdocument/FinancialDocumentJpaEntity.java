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
 * JPA ORM 엔티티 — bms.financial_document.
 * document_status는 String으로 매핑(DB 저장값 = enum.name()).
 */
@Entity
@Table(schema = "bms", name = "financial_document")
@Getter
@NoArgsConstructor
public class FinancialDocumentJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "financial_document_id", updatable = false, nullable = false)
    private Long financialDocumentId;

    @Column(name = "document_no", nullable = false, length = 20)
    private String documentNo;

    @Column(name = "document_type", nullable = false, length = 10)
    private String documentType;

    @Column(name = "document_dt", length = 8)
    private String documentDt;

    @Column(name = "document_status", nullable = false, length = 10)
    private String documentStatus;

    @Column(name = "group_financial_no", length = 20)
    private String groupFinancialNo;

    @Column(name = "customer_code", length = 40)
    private String customerCode;

    @Column(name = "settle_total_amount", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal settleTotalAmount;

    @Column(name = "local_total_amount", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal localTotalAmount;

    @Column(name = "settle_total_vat", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal settleTotalVat;

    @Column(name = "local_total_vat", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal localTotalVat;

    @Column(name = "usd_total_amount", columnDefinition = "NUMERIC(18,2)")
    private BigDecimal usdTotalAmount;

    @Column(name = "performance_dt", length = 8)
    private String performanceDt;

    @Column(name = "team_code", length = 40)
    private String teamCode;

    @Column(name = "operator", length = 50)
    private String operator;

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setDocumentNo(String v) { this.documentNo = v; }
    public void setDocumentType(String v) { this.documentType = v; }
    public void setDocumentDt(String v) { this.documentDt = v; }
    public void setDocumentStatus(String v) { this.documentStatus = v; }
    public void setGroupFinancialNo(String v) { this.groupFinancialNo = v; }
    public void setCustomerCode(String v) { this.customerCode = v; }
    public void setSettleTotalAmount(BigDecimal v) { this.settleTotalAmount = v; }
    public void setLocalTotalAmount(BigDecimal v) { this.localTotalAmount = v; }
    public void setSettleTotalVat(BigDecimal v) { this.settleTotalVat = v; }
    public void setLocalTotalVat(BigDecimal v) { this.localTotalVat = v; }
    public void setUsdTotalAmount(BigDecimal v) { this.usdTotalAmount = v; }
    public void setPerformanceDt(String v) { this.performanceDt = v; }
    public void setTeamCode(String v) { this.teamCode = v; }
    public void setOperator(String v) { this.operator = v; }
}
