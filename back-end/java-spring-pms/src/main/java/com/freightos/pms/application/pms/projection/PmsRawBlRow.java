package com.freightos.pms.application.pms.projection;

import java.math.BigDecimal;

/**
 * 집계 쿼리 원시 결과 한 행. 화물/이름 정보는 미포함(서비스 계층에서 병합).
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
    BigDecimal creditUsdAmt
) {}
