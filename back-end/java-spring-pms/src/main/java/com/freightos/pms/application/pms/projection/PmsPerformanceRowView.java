package com.freightos.pms.application.pms.projection;

import java.math.BigDecimal;

/**
 * PMS 실적 조회 결과 한 행(36컬럼). 집계 단위 = House B/L.
 * MASTER 행의 경우 House B/L No는 공란이고 masterBlNo만 채워진다.
 */
public record PmsPerformanceRowView(

    // 식별 · B/L 속성 (col 1-18)
    String blType,
    Long blId,
    String houseBlNo,           // col 1
    String masterBlNo,          // col 2
    String teamCode,            // col 3 (code)
    String teamName,            // col 3 (name)
    String jobDiv,              // col 4
    String bound,               // col 5
    String etd,                 // col 6 yyyyMMdd
    String eta,                 // col 7 yyyyMMdd
    String performanceDt,       // col 8 yyyyMMdd (최신)
    String actualCustomerCode,  // col 9
    String actualCustomerName,  // col 10
    String settlePartnerCode,   // col 11
    String settlePartnerName,   // col 12
    String linerCode,           // col 13
    String linerName,           // col 14
    String polCode,             // col 15
    String podCode,             // col 16
    String salesManCode,        // col 17 (code)
    String salesManName,        // col 17 (name)
    String incoterms,           // col 18

    // 화물 수치 (col 19-24)
    String loadType,            // col 19
    Integer pkgQty,             // col 20
    BigDecimal rton,            // col 21 (SEA=derived, NON_BL=stored, AIR/TRUCK=null)
    BigDecimal cbm,             // col 22
    BigDecimal chargeWeightKg,  // col 23 (AIR/TRUCK)
    BigDecimal grossWeightKg,   // col 24

    // 금액 (col 25-34)
    BigDecimal invoiceLocalAmt,  // col 25
    BigDecimal debitLocalAmt,    // col 26
    BigDecimal paymentLocalAmt,  // col 27
    BigDecimal creditLocalAmt,   // col 28
    BigDecimal localProfit,      // col 29 = (invoice+debit)-(payment+credit)
    BigDecimal invoiceUsdAmt,    // col 30
    BigDecimal debitUsdAmt,      // col 31
    BigDecimal paymentUsdAmt,    // col 32
    BigDecimal creditUsdAmt,     // col 33
    BigDecimal usdProfit,        // col 34

    // 마감 (col 35-36) — 추후 기능, 현재 공란
    String blClosed,            // col 35
    String freightClosed        // col 36
) {}
