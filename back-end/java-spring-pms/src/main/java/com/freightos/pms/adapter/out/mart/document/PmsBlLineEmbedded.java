package com.freightos.pms.adapter.out.mart.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 페이지 금액 재집계용 freight_line 원본 임베디드. PmsBlMartDocument.lines[] 원소.
 * @Document 아님 — 순수 임베디드 값 객체.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PmsBlLineEmbedded {

    /** performance_dt (yyyyMMdd, nullable). */
    private String pd;

    /** financial_doc_type (INVOICE/DEBIT/PAYMENT/CREDIT). */
    private String fdcType;

    /** tax_type. */
    private String taxType;

    /** tax_no IS NOT NULL. */
    private boolean tax;

    /** slip_no IS NOT NULL. */
    private boolean slip;

    /** financial_document_id IS NOT NULL. */
    private boolean issued;

    /** local_amount. */
    private BigDecimal local;

    /** usd_amount. */
    private BigDecimal usd;
}
