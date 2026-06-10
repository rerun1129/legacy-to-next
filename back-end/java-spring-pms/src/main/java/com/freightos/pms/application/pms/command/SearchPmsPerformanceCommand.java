package com.freightos.pms.application.pms.command;

import com.freightos.pms.application.pms.AggregationBasis;

import java.util.List;

/**
 * PMS 실적 조회 커맨드. 필터 필드는 null/blank = 무시.
 * FE가 실제로 전송하는 필터 + 기간 파생 필드만 보유한다.
 * 다중 선택(documentTypes)은 빈 리스트 = 전체.
 */
public record SearchPmsPerformanceCommand(

    // ── 집계 기준 ────────────────────────────────────────────────────────────
    AggregationBasis basis,          // null 시 FREIGHT_INPUT 기본

    // ── 페이지 ───────────────────────────────────────────────────────────────
    Integer page,
    Integer size,

    // ── B/L 공통 필터 ────────────────────────────────────────────────────────
    String jobDiv,                   // SEA/AIR/TRUCK/NON_BL
    String bound,                    // EXP/IMP
    String dateKind,                 // ETD/ETA (그 외 값은 무시)
    String dateFrom,
    String dateTo,
    String performanceDtFrom,
    String performanceDtTo,

    // ── BMS 서류 필터 ────────────────────────────────────────────────────────
    String documentDtFrom,
    String documentDtTo,
    List<String> documentTypes,      // INVOICE/PAYMENT/DEBIT/CREDIT (다중)
    String documentStatus,

    // ── 총건수 정확도 ─────────────────────────────────────────────────────────
    Boolean exactCount,          // null/false = 근사(기본), true = 정확

    // ── 캐시 무효화 토큰 ─────────────────────────────────────────────────────
    Long searchNonce             // Search 시 새 값 → 시그니처 변경 → 캐시 미스. 페이지 이동은 동일값 유지
) {

    /** basis가 null이면 FREIGHT_INPUT으로 정규화. */
    public AggregationBasis effectiveBasis() {
        return basis != null ? basis : AggregationBasis.FREIGHT_INPUT;
    }
}
