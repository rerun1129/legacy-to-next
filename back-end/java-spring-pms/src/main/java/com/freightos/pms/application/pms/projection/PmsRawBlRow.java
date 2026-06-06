package com.freightos.pms.application.pms.projection;

import java.math.BigDecimal;

/**
 * 집계 쿼리 단일 결과 한 행.
 * page CTE + identity/cargo/name LEFT JOIN 단일 쿼리에서 직접 채워진다.
 * Phase-2 keyed lookup 제거 후 모든 필드가 한 번에 반환됨.
 */
public record PmsRawBlRow(

    // B/L 그룹 키
    String blType,
    Long blId,

    // B/L 식별
    String houseBlNo,
    String masterBlNo,
    String jobDiv,
    String bound,
    String etd,
    String eta,
    String performanceDt,

    // 헤더 출처 코드
    String actualCustomerCode,
    String settlePartnerCode,
    String linerCode,
    String polCode,
    String podCode,
    String salesManCode,
    String incoterms,
    Long houseBlId,          // null when blType=MASTER

    // 서류/팀/오퍼레이터 (freight_line 또는 document 출처)
    String teamCode,
    String operator,

    // 금액 집계 (doc_type별)
    BigDecimal invoiceLocalAmt,
    BigDecimal debitLocalAmt,
    BigDecimal paymentLocalAmt,
    BigDecimal creditLocalAmt,
    BigDecimal invoiceUsdAmt,
    BigDecimal debitUsdAmt,
    BigDecimal paymentUsdAmt,
    BigDecimal creditUsdAmt,

    // cargo 수치 (HOUSE만 의미 있음, MASTER는 null)
    Integer pkgQty,
    BigDecimal cbm,
    BigDecimal grossWeightKg,
    String seaLoadType,
    BigDecimal airChargeWeightKg,
    BigDecimal truckChargeWeightKg,
    String truckLoadType,
    BigDecimal nonBlRton,

    // 코드명 (단일 쿼리 JOIN에서 직접 해소)
    String accName,
    String spcName,
    String lcName,
    String teamName,
    String salesManName
) {

    /**
     * 기존 26-필드 생성자 호환(Phase-2 제거 전 코드가 사용하는 경로 — 현재는 사용되지 않음).
     * 신규 필드는 null로 초기화.
     */
    public PmsRawBlRow(
            String blType, Long blId,
            String houseBlNo, String masterBlNo, String jobDiv, String bound, String etd, String eta,
            String performanceDt,
            String actualCustomerCode, String settlePartnerCode, String linerCode,
            String polCode, String podCode, String salesManCode, String incoterms,
            Long houseBlId,
            String teamCode, String operator,
            BigDecimal invoiceLocalAmt, BigDecimal debitLocalAmt,
            BigDecimal paymentLocalAmt, BigDecimal creditLocalAmt,
            BigDecimal invoiceUsdAmt, BigDecimal debitUsdAmt,
            BigDecimal paymentUsdAmt, BigDecimal creditUsdAmt) {
        this(blType, blId,
             houseBlNo, masterBlNo, jobDiv, bound, etd, eta, performanceDt,
             actualCustomerCode, settlePartnerCode, linerCode,
             polCode, podCode, salesManCode, incoterms, houseBlId,
             teamCode, operator,
             invoiceLocalAmt, debitLocalAmt, paymentLocalAmt, creditLocalAmt,
             invoiceUsdAmt, debitUsdAmt, paymentUsdAmt, creditUsdAmt,
             null, null, null, null, null, null, null, null,
             null, null, null, null, null);
    }
}
