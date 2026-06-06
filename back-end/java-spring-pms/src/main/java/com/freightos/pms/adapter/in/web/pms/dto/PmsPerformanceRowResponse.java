package com.freightos.pms.adapter.in.web.pms.dto;

import java.math.BigDecimal;

/**
 * PS-01 실적 그리드 한 행 응답 DTO. 36컬럼.
 */
public record PmsPerformanceRowResponse(

    // 식별
    String blType,
    Long blId,

    // col 1-2
    String houseBlNo,
    String masterBlNo,

    // col 3 Team
    String teamCode,
    String teamName,

    // col 4-8 B/L 속성
    String jobDiv,
    String bound,
    String etd,
    String eta,
    String performanceDt,

    // col 9-10 Actual Customer
    String actualCustomerCode,
    String actualCustomerName,

    // col 11-12 Settle Partner
    String settlePartnerCode,
    String settlePartnerName,

    // col 13-14 Carrier
    String linerCode,
    String linerName,

    // col 15-16 항만
    String polCode,
    String podCode,

    // col 17 Sales Man
    String salesManCode,
    String salesManName,

    // col 18
    String incoterms,

    // col 19-24 화물 수치
    String loadType,
    Integer pkgQty,
    BigDecimal rton,
    BigDecimal cbm,
    BigDecimal chargeWeightKg,
    BigDecimal grossWeightKg,

    // col 25-29 Local 금액
    BigDecimal invoiceLocalAmt,
    BigDecimal debitLocalAmt,
    BigDecimal paymentLocalAmt,
    BigDecimal creditLocalAmt,
    BigDecimal localProfit,

    // col 30-34 USD 금액
    BigDecimal invoiceUsdAmt,
    BigDecimal debitUsdAmt,
    BigDecimal paymentUsdAmt,
    BigDecimal creditUsdAmt,
    BigDecimal usdProfit,

    // col 35-36 마감 (공란)
    String blClosed,
    String freightClosed
) {}
