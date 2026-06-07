package com.freightos.pms.adapter.out.mart.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DOCUMENT_CREATED basis 전용 임베디드.
 * BasisAggEmbedded 공통 필드를 모두 포함하되, 서류 특화 필드(docCount/팀/오퍼레이터)를
 * 추가로 보유한다. 상속 대신 독립 클래스(필드 평탄화)로 구현 — MongoDB 문서 내
 * 경로 접근이 단순하고 스키마 변경 충격이 격리된다.
 * @Document 아님 — 순수 임베디드 값 객체.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentAggEmbedded {

    /** 기준일 최댓값(yyyyMMdd 문자열). */
    private String performanceDt;

    /** financial_document 행 수. */
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

    // DOCUMENT_CREATED 전용 추가 필드
    /** 서류 건수(financial_document.document_id 기준). */
    private long docCount;

    /** MAX(financial_document.team_code). */
    private String teamCode;

    /** resolve된 팀 이름. */
    private String teamName;

    /** MAX(financial_document.operator). */
    private String operator;
}
