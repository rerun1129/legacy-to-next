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
    String taxType,

    // ── 총건수 정확도 ─────────────────────────────────────────────────────────
    Boolean exactCount,          // null/false = 근사(기본), true = 정확

    // ── 캐시 무효화 토큰 ─────────────────────────────────────────────────────
    Long searchNonce             // Search 시 새 값 → 시그니처 변경 → 캐시 미스. 페이지 이동은 동일값 유지
) {
    /**
     * searchNonce 이전 호출부·테스트가 positional 생성자로 searchNonce 없이 생성할 수 있도록
     * 하위호환 편의 생성자를 제공한다. searchNonce=null 위임.
     */
    public SearchPmsPerformanceCommand(
        AggregationBasis basis, Integer page, Integer size,
        String jobDiv, String bound, String dateKind, String dateFrom, String dateTo,
        String performanceDtFrom, String performanceDtTo, String hblNo, String mblNo,
        String partyKind, String partyCode, String actualCustomerCode, String settlePartnerCode,
        String carrierCode, String portKind, String portCode,
        String salesManCode, String salesClass, String incoterms,
        String vesselVoyage, String loadType, String teamCode, String operator,
        List<String> documentTypes, String documentStatus, String documentNoLike,
        String documentDtFrom, String documentDtTo, String groupFinancialNo,
        String grouped, String issued, String financialDocType, String taxType,
        Boolean exactCount
    ) {
        this(basis, page, size,
            jobDiv, bound, dateKind, dateFrom, dateTo,
            performanceDtFrom, performanceDtTo, hblNo, mblNo,
            partyKind, partyCode, actualCustomerCode, settlePartnerCode,
            carrierCode, portKind, portCode,
            salesManCode, salesClass, incoterms,
            vesselVoyage, loadType, teamCode, operator,
            documentTypes, documentStatus, documentNoLike,
            documentDtFrom, documentDtTo, groupFinancialNo,
            grouped, issued, financialDocType, taxType,
            exactCount, null);
    }

    /** basis가 null이면 FREIGHT_INPUT으로 정규화. */
    public AggregationBasis effectiveBasis() {
        return basis != null ? basis : AggregationBasis.FREIGHT_INPUT;
    }
}
