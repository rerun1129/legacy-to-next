package com.freightos.pms.adapter.out.mart.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * FREIGHT_INPUT / TAX_ISSUED / SLIP_ISSUED basis 공용 임베디드.
 * PmsBlMartDocument 내 freightInput·taxIssued·slipIssued 3 블록에서 재사용된다.
 * @Document 아님 — 순수 임베디드 값 객체.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BasisAggEmbedded {

    /** 기준일 최댓값(yyyyMMdd 문자열). */
    private String performanceDt;

    /** 해당 basis에 속하는 freight_line 행 수. */
    private long lineCount;

    // 로컬 통화 집계
    private BigDecimal invoiceLocalAmt;
    private BigDecimal debitLocalAmt;
    private BigDecimal paymentLocalAmt;
    private BigDecimal creditLocalAmt;

    // USD 집계
    private BigDecimal invoiceUsdAmt;
    private BigDecimal debitUsdAmt;
    private BigDecimal paymentUsdAmt;
    private BigDecimal creditUsdAmt;
}
