package com.freightos.pms.adapter.in.web.pms.dto;

import java.util.List;

/**
 * POST /api/pms/performance/search 요청 DTO.
 * FE가 실제로 전송하는 9개 필터만 포함한다.
 * basis 미지정 시 FREIGHT_INPUT 기본.
 */
public record SearchPmsPerformanceRequest(

    // 집계 기준
    String basis,

    // 페이지
    Integer page,
    Integer size,

    // B/L 공통
    String jobDiv,
    String bound,

    // 기간 (ETD·ETA 시 dateFrom/To, 실적=performanceDtFrom/To, 서류=documentDtFrom/To — 한 쌍만)
    String dateKind,
    String dateFrom,
    String dateTo,
    String performanceDtFrom,
    String performanceDtTo,
    String documentDtFrom,
    String documentDtTo,

    // BMS 서류
    List<String> documentTypes,
    String documentStatus,
    String grouped,
    String issued,

    // 총건수 정확도: null/false = 근사(기본), true = 정확
    Boolean exactCount,

    // BE 조회 캐시 무효화용 Search 토큰 — 페이지 이동 시 동일값 유지, Search 시 새 값 전송
    Long searchNonce
) {}
