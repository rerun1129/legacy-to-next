package com.freightos.pms.adapter.in.web.pms.dto;

import java.util.List;

/**
 * POST /api/pms/performance/search 요청 DTO.
 * 모든 필드는 선택(null = 무시). basis 미지정 시 FREIGHT_INPUT 기본.
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
    String dateKind,
    String dateFrom,
    String dateTo,
    String performanceDtFrom,
    String performanceDtTo,
    String hblNo,
    String mblNo,

    // 거래처
    String partyKind,
    String partyCode,
    String actualCustomerCode,
    String settlePartnerCode,

    // 운송사
    String carrierCode,

    // 항만
    String portKind,
    String portCode,

    // 영업
    String salesManCode,
    String salesClass,
    String incoterms,
    String vesselVoyage,
    String loadType,
    String teamCode,
    String operator,

    // BMS 서류
    List<String> documentTypes,
    String documentStatus,
    String documentNoLike,
    String documentDtFrom,
    String documentDtTo,
    String groupFinancialNo,
    String grouped,
    String issued,

    // BMS 운임행
    String financialDocType,
    String taxType,

    // 총건수 정확도: null/false = 근사(기본), true = 정확
    Boolean exactCount
) {}
