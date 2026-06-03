package com.freightos.fms.domain.freight;

import com.freightos.fms.domain.freight.enums.FreightType;
import com.freightos.fms.domain.freight.enums.TaxType;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 운임 라인 계산 순수 함수 모음.
 * I/O 없음 — 입력값으로만 계산 결과를 반환한다.
 * 금액 scale=2 RoundingMode.HALF_UP, 환율 scale=6.
 * null 입력은 0(또는 skip)으로 처리 — 밸리데이션은 BE-B.
 */
public final class FreightCalculator {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.10");
    private static final int AMOUNT_SCALE = 2;
    private static final int RATE_SCALE = 6;

    private FreightCalculator() {}

    /**
     * settleAmount = unitQuantity × unitPrice.
     * 어느 한쪽이 null 이면 ZERO 반환.
     */
    public static BigDecimal calcSettleAmount(BigDecimal unitQuantity, BigDecimal unitPrice) {
        if (unitQuantity == null || unitPrice == null) return BigDecimal.ZERO;
        return unitQuantity.multiply(unitPrice).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * exchangeRate = freightType==SELLING ? header.sellRate : header.buyRate.
     * null 이면 ZERO 반환.
     */
    public static BigDecimal resolveExchangeRate(FreightType freightType, BigDecimal sellRate, BigDecimal buyRate) {
        if (freightType == FreightType.SELLING) return sellRate != null ? sellRate.setScale(RATE_SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return buyRate != null ? buyRate.setScale(RATE_SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;
    }

    /**
     * localAmount = settleAmount × exchangeRate.
     * null 이면 ZERO 반환.
     */
    public static BigDecimal calcLocalAmount(BigDecimal settleAmount, BigDecimal exchangeRate) {
        if (settleAmount == null || exchangeRate == null) return BigDecimal.ZERO;
        return settleAmount.multiply(exchangeRate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * usdAmount = settleAmount × usdRate.
     * null 이면 ZERO 반환.
     */
    public static BigDecimal calcUsdAmount(BigDecimal settleAmount, BigDecimal usdRate) {
        if (settleAmount == null || usdRate == null) return BigDecimal.ZERO;
        return settleAmount.multiply(usdRate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * settleTaxAmount = (taxType==TAXABLE ? settleAmount × 0.10 : ZERO).
     * null 이면 ZERO 반환.
     */
    public static BigDecimal calcSettleTaxAmount(TaxType taxType, BigDecimal settleAmount) {
        if (taxType != TaxType.TAXABLE || settleAmount == null) return BigDecimal.ZERO;
        return settleAmount.multiply(TAX_RATE).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * localTaxAmount = (taxType==TAXABLE ? localAmount × 0.10 : ZERO).
     * null 이면 ZERO 반환.
     */
    public static BigDecimal calcLocalTaxAmount(TaxType taxType, BigDecimal localAmount) {
        if (taxType != TaxType.TAXABLE || localAmount == null) return BigDecimal.ZERO;
        return localAmount.multiply(TAX_RATE).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 라인 한 건의 모든 계산값을 한 번에 채운다.
     * FreightLine의 입력값(freightType, unitQuantity, unitPrice, taxType)과
     * FreightHeader의 환율(sellRate, buyRate, usdRate)을 받아 line에 직접 설정한다.
     */
    public static void applyCalculations(FreightLine line, BigDecimal sellRate, BigDecimal buyRate, BigDecimal usdRate) {
        BigDecimal exchangeRate = resolveExchangeRate(line.getFreightType(), sellRate, buyRate);
        BigDecimal settleAmount = calcSettleAmount(line.getUnitQuantity(), line.getUnitPrice());
        BigDecimal localAmount = calcLocalAmount(settleAmount, exchangeRate);
        BigDecimal usdExchangeRate = usdRate != null ? usdRate.setScale(RATE_SCALE, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        BigDecimal usdAmount = calcUsdAmount(settleAmount, usdRate);
        BigDecimal settleTaxAmount = calcSettleTaxAmount(line.getTaxType(), settleAmount);
        BigDecimal localTaxAmount = calcLocalTaxAmount(line.getTaxType(), localAmount);

        line.setExchangeRate(exchangeRate);
        line.setSettleAmount(settleAmount);
        line.setLocalAmount(localAmount);
        line.setUsdExchangeRate(usdExchangeRate);
        line.setUsdAmount(usdAmount);
        line.setSettleTaxAmount(settleTaxAmount);
        line.setLocalTaxAmount(localTaxAmount);
    }
}
