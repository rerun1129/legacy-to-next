package com.freightos.pms.adapter.out.mart.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * PMS B/L 단위 사전집계 Mart 문서.
 * 컬렉션: pms_bl_mart.
 *
 * id = blType + "#" + blId 복합 문자열 키.
 * MASTER 행은 house 전용 필드(houseBlId, houseBlNo, salesManCode, incoterms, salesClass,
 * houseTeamCode, cargo 블록 전체)가 null이다.
 *
 * 금액 필드는 java.math.BigDecimal — Spring Data MongoDB(Boot 3.4)가
 * BigDecimal ↔ Decimal128 자동 변환을 제공하므로 별도 컨버터 없이 정확 십진수가 보존된다.
 *
 * basis 블록(freightInput/taxIssued/slipIssued/documentCreated)은 데이터가 없어도
 * 항상 non-null 인스턴스로 저장(금액 0·count 0). 노출 여부는 hasFoo 플래그로 판단한다.
 */
@Document("pms_bl_mart")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PmsBlMartDocument {

    // ── 키 / 식별 ────────────────────────────────────────────────────────────

    /** blType + "#" + blId 복합 키. */
    @Id
    private String id;

    private String blType;
    private Long blId;

    /** MASTER 행일 때 null. */
    private Long houseBlId;

    // ── B/L 식별자 ──────────────────────────────────────────────────────────

    /** MASTER 행일 때 null. */
    private String houseBlNo;

    private String masterBlNo;
    private String jobDiv;
    private String bound;

    /** yyyyMMdd 문자열. */
    private String etd;

    /** yyyyMMdd 문자열. */
    private String eta;

    private String polCode;
    private String podCode;
    private String actualCustomerCode;
    private String settlePartnerCode;
    private String linerCode;

    /** HOUSE 전용. MASTER일 때 null. */
    private String salesManCode;

    /** HOUSE 전용. MASTER일 때 null. */
    private String incoterms;

    /** HOUSE 전용. MASTER일 때 null. */
    private String salesClass;

    /** house_bl.team_code. HOUSE 전용. MASTER일 때 null. */
    private String houseTeamCode;

    // ── resolve된 코드명 ────────────────────────────────────────────────────

    /** actualCustomerCode 이름. */
    private String accName;

    /** settlePartnerCode 이름. */
    private String spcName;

    /** linerCode 이름. */
    private String lcName;

    /** HOUSE 전용. MASTER일 때 null. */
    private String houseTeamName;

    /** HOUSE 전용. MASTER일 때 null. */
    private String salesManName;

    // ── cargo (HOUSE 전용, MASTER 전부 null) ────────────────────────────────

    private Integer pkgQty;
    private BigDecimal cbm;
    private BigDecimal grossWeightKg;
    private String seaLoadType;
    private BigDecimal airChargeWeightKg;
    private BigDecimal truckChargeWeightKg;
    private String truckLoadType;
    private BigDecimal nonBlRton;

    // ── basis 존재 플래그 (인덱스 선택성 보조) ──────────────────────────────

    private boolean hasFreightInput;
    private boolean hasTaxIssued;
    private boolean hasSlipIssued;
    private boolean hasDocumentCreated;

    // ── basis 집계 블록 ─────────────────────────────────────────────────────

    private BasisAggEmbedded freightInput;
    private BasisAggEmbedded taxIssued;
    private BasisAggEmbedded slipIssued;
    private DocumentAggEmbedded documentCreated;

    // ── Mart 메타 ───────────────────────────────────────────────────────────

    /** 이 문서가 마지막으로 갱신된 시각. */
    private Instant martUpdatedAt;

    // ── line-grain 원본 배열 (페이지 금액 재집계용 — Phase 2 소비. line-accel ON ETL에서만 채움) ──
    private List<PmsBlLineEmbedded> lines;
    private List<PmsBlDocEmbedded> docs;
}
