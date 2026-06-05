package com.freightos.fms.domain.freight;

import com.freightos.fms.domain.freight.enums.FreightBlType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 운임 헤더 도메인 엔티티.
 * B/L 1:1 대응, 환율 3계열(매출/매입/USD) 보유.
 * 순수 Java — Spring/JPA import 없음.
 */
public class FreightHeader {

    private FreightBlType blType;
    private Long blId;

    // ── 당사자 3종 ─────────────────────────────────────────────────────────────
    private String actualCustomerCode;
    private String linerCode;
    private String settlePartnerCode;

    // ── 환율 매출 계열 ─────────────────────────────────────────────────────────
    private String sellRateDt;
    private String sellRateCurrencyCode;
    private BigDecimal sellRate;

    // ── 환율 매입 계열 ─────────────────────────────────────────────────────────
    private String buyRateDt;
    private String buyRateCurrencyCode;
    private BigDecimal buyRate;

    // ── 환율 USD 계열 (currencyCode 없음 — DDL 확인) ───────────────────────────
    private String usdRateDt;
    private BigDecimal usdRate;

    private List<FreightLine> lines = new ArrayList<>();

    public FreightHeader() {}

    // ── Getters ────────────────────────────────────────────────────────────────

    public FreightBlType getBlType() { return blType; }
    public Long getBlId() { return blId; }
    public String getActualCustomerCode() { return actualCustomerCode; }
    public String getLinerCode() { return linerCode; }
    public String getSettlePartnerCode() { return settlePartnerCode; }
    public String getSellRateDt() { return sellRateDt; }
    public String getSellRateCurrencyCode() { return sellRateCurrencyCode; }
    public BigDecimal getSellRate() { return sellRate; }
    public String getBuyRateDt() { return buyRateDt; }
    public String getBuyRateCurrencyCode() { return buyRateCurrencyCode; }
    public BigDecimal getBuyRate() { return buyRate; }
    public String getUsdRateDt() { return usdRateDt; }
    public BigDecimal getUsdRate() { return usdRate; }
    public List<FreightLine> getLines() { return lines; }

    // ── Setters ────────────────────────────────────────────────────────────────

    public void setBlType(FreightBlType v) { this.blType = v; }
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
    public void setLines(List<FreightLine> v) { this.lines = v; }
}
