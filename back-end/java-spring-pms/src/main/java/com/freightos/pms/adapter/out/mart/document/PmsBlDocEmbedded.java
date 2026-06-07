package com.freightos.pms.adapter.out.mart.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 페이지 금액 재집계용 financial_document 원본 임베디드(distinct fan-out 방지 단위). PmsBlMartDocument.docs[] 원소.
 * @Document 아님 — 순수 임베디드 값 객체.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PmsBlDocEmbedded {

    /** financial_document_id. */
    private Long fdId;

    /** financial_document.performance_dt. */
    private String perfPd;

    /** financial_document.document_dt. */
    private String docDt;

    /** document_type. */
    private String docType;

    /** document_status. */
    private String status;

    /** group_financial_no IS NOT NULL. */
    private boolean grouped;

    /** team_code. */
    private String team;

    /** admin.team.name (team_code에 함수종속 — DISTINCT 카디널리티 불변). */
    private String teamName;

    /** operator. */
    private String operator;

    /** local_total_amount. */
    private BigDecimal local;

    /** usd_total_amount. */
    private BigDecimal usd;
}
