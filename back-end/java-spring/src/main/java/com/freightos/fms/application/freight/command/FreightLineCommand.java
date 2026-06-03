package com.freightos.fms.application.freight.command;

import java.math.BigDecimal;

/**
 * 운임 라인 저장 커맨드 — 입력값 + FE 실시간 계산값 포함.
 * BE는 재계산 없이 FE가 보낸 계산값을 그대로 저장한다.
 * financialDocType은 non-blank면 검증 후 사용, blank/null이면 FinancialDocTypePolicy 폴백.
 */
public record FreightLineCommand(
    /** 매출/매입 구분 (name() 문자열) */
    String freightType,
    String freightCode,
    /** Per 기준 — Per 코드 또는 컨테이너 타입 코드 혼재(§6.6). */
    String per,
    BigDecimal unitQuantity,
    BigDecimal unitPrice,
    String currency,
    String customerCode,
    /** 세금 유형 (name() 문자열) */
    String taxType,
    /** 실적 인정 일자 (yyyyMMdd) */
    String performanceDt,

    // ── FE 실시간 계산값 (BE는 그대로 저장) ───────────────────────────────────
    BigDecimal exchangeRate,
    BigDecimal settleAmount,
    BigDecimal localAmount,
    BigDecimal usdExchangeRate,
    BigDecimal usdAmount,
    BigDecimal localTaxAmount,
    /** 금융 서류 종류 (name() 문자열). null/blank면 FinancialDocTypePolicy 폴백. */
    String financialDocType
) {

    /**
     * 하위호환 생성자 — 계산값 미포함(기존 CommandBuilder 호출부 보존용).
     * 계산값은 모두 null로 초기화되며, BE 저장 시 null 그대로 저장된다.
     */
    public FreightLineCommand(
            String freightType,
            String freightCode,
            String per,
            BigDecimal unitQuantity,
            BigDecimal unitPrice,
            String currency,
            String customerCode,
            String taxType,
            String performanceDt) {
        this(freightType, freightCode, per, unitQuantity, unitPrice, currency, customerCode, taxType, performanceDt,
                null, null, null, null, null, null, null);
    }
}
