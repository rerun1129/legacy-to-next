package com.freightos.pms.application.pms.command;

import com.freightos.pms.application.pms.AggregationBasis;

import java.util.List;

/**
 * PMS 실적 조회 커맨드. 필터 필드는 null/blank = 무시.
 * 다중 선택(documentTypes)은 빈 리스트 = 전체.
 */
public record SearchPmsPerformanceCommand(

    // ── 집계 기준 ────────────────────────────────────────────────────────────
    AggregationBasis basis,          // null 시 FREIGHT_INPUT 기본

    // ── 페이지 ───────────────────────────────────────────────────────────────
    Integer page,
    Integer size,

    // ── FMS B/L 공통 필터 ────────────────────────────────────────────────────
    String jobDiv,                   // SEA/AIR/TRUCK/NON_BL
    String bound,                    // EXP/IMP
    String dateKind,                 // ETD/ETA/PERFORMANCE
    String dateFrom,
    String dateTo,
    String performanceDtFrom,
    String performanceDtTo,
    String hblNo,
    String mblNo,

    // ── 거래처 ───────────────────────────────────────────────────────────────
    String partyKind,                // ACTUAL_CUSTOMER/SETTLE_PARTNER
    String partyCode,
    String actualCustomerCode,
    String settlePartnerCode,

    // ── 운송사 ───────────────────────────────────────────────────────────────
    String carrierCode,

    // ── 항만 ─────────────────────────────────────────────────────────────────
    String portKind,                 // POL/POD
    String portCode,

    // ── 영업 ─────────────────────────────────────────────────────────────────
    String salesManCode,
    String salesClass,
    String incoterms,
    String vesselVoyage,
    String loadType,
    String teamCode,
    String operator,

    // ── BMS 서류 필터 ────────────────────────────────────────────────────────
    List<String> documentTypes,      // INVOICE/PAYMENT/DEBIT/CREDIT (다중)
    String documentStatus,
    String documentNoLike,
    String documentDtFrom,
    String documentDtTo,
    String groupFinancialNo,
    String grouped,                  // Y/N
    String issued,                   // Y/N (freight_line.financial_document_id IS (NOT) NULL)

    // ── BMS 운임행 필터 ──────────────────────────────────────────────────────
    String financialDocType,
    String taxType
) {
    /** basis가 null이면 FREIGHT_INPUT으로 정규화. */
    public AggregationBasis effectiveBasis() {
        return basis != null ? basis : AggregationBasis.FREIGHT_INPUT;
    }
}
