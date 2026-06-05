package com.freightos.fms.adapter.out.persistence.freight;

import com.freightos.common.persistence.BaseJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA ORM 엔티티 — bms.freight_header.
 * B/L 1:1(UNIQUE: bl_type+bl_id), 라인은 @OneToMany(cascade ALL, orphanRemoval).
 */
@Entity
@Table(schema = "bms", name = "freight_header")
@Getter
@NoArgsConstructor
public class FreightHeaderJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freight_header_id", updatable = false, nullable = false)
    private Long freightHeaderId;

    @Column(name = "bl_type", nullable = false, length = 10)
    private String blType;

    @Column(name = "bl_id", nullable = false)
    private Long blId;

    // ── 당사자 3종 ─────────────────────────────────────────────────────────────
    @Column(name = "actual_customer_code", length = 40)
    private String actualCustomerCode;

    @Column(name = "liner_code", length = 40)
    private String linerCode;

    @Column(name = "settle_partner_code", length = 40)
    private String settlePartnerCode;

    // ── 환율 매출 계열 ─────────────────────────────────────────────────────────
    @Column(name = "sell_rate_dt", length = 8)
    private String sellRateDt;

    @Column(name = "sell_rate_currency_code", length = 10)
    private String sellRateCurrencyCode;

    @Column(name = "sell_rate", columnDefinition = "NUMERIC(18,6)")
    private BigDecimal sellRate;

    // ── 환율 매입 계열 ─────────────────────────────────────────────────────────
    @Column(name = "buy_rate_dt", length = 8)
    private String buyRateDt;

    @Column(name = "buy_rate_currency_code", length = 10)
    private String buyRateCurrencyCode;

    @Column(name = "buy_rate", columnDefinition = "NUMERIC(18,6)")
    private BigDecimal buyRate;

    // ── 환율 USD 계열 (currencyCode 없음 — DDL usd_rate_currency_code 컬럼 미존재) ─
    @Column(name = "usd_rate_dt", length = 8)
    private String usdRateDt;

    @Column(name = "usd_rate", columnDefinition = "NUMERIC(18,6)")
    private BigDecimal usdRate;

    // ── 라인 컬렉션 ───────────────────────────────────────────────────────────
    @OneToMany(mappedBy = "freightHeader", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    private List<FreightLineJpaEntity> lines = new ArrayList<>();

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setBlType(String v) { this.blType = v; }
    public void setBlId(Long v) { this.blId = v; }
    public void setActualCustomerCode(String v) { this.actualCustomerCode = v; }
    public void setLinerCode(String v) { this.linerCode = v; }
    public void setSettlePartnerCode(String v) { this.settlePartnerCode = v; }
    public void setSellRateDt(String v) { this.sellRateDt = v; }
    public void setSellRateCurrencyCode(String v) { this.sellRateCurrencyCode = v; }
    public void setSellRate(BigDecimal v) { this.sellRate = v; }
    public void setBuyRateDt(String v) { this.buyRateDt = v; }
    public void setBuyRateCurrencyCode(String v) { this.buyRateCurrencyCode = v; }
    public void setBuyRate(BigDecimal v) { this.buyRate = v; }
    public void setUsdRateDt(String v) { this.usdRateDt = v; }
    public void setUsdRate(BigDecimal v) { this.usdRate = v; }

    /**
     * 라인 전량 재구성: 기존 컬렉션 내용을 교체하여 orphanRemoval이 동작하도록 한다.
     * newLines의 각 FreightLineJpaEntity는 freightHeader 참조가 이미 설정된 상태여야 한다.
     */
    public void syncLines(List<FreightLineJpaEntity> newLines) {
        this.lines.clear();
        this.lines.addAll(newLines);
    }

    /**
     * 미발행 라인만 교체한다.
     * financial_document_id != null 인 발행 라인은 컬렉션에 그대로 유지하여
     * orphanRemoval DELETE 위험을 차단한다. 미발행 라인만 제거 후 newLines를 추가한다.
     * newLines는 미발행 재구성분(command 기반)이어야 한다.
     */
    public void syncUnissuedLines(List<FreightLineJpaEntity> newLines) {
        this.lines.removeIf(line -> line.getFinancialDocumentId() == null);
        this.lines.addAll(newLines);
    }
}
