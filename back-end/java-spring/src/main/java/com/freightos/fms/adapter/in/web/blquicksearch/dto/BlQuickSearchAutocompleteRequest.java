package com.freightos.fms.adapter.in.web.blquicksearch.dto;

/**
 * BL 자동완성 검색 요청 DTO.
 * 모든 필드는 optional이며 검증 어노테이션 없음 — BE 검증 없이 그대로 커맨드로 전달.
 * q: blNo 검색어. 공란이면 전체 자동완성.
 */
public record BlQuickSearchAutocompleteRequest(
    String q,
    Integer limit,
    String jobDiv,
    String bound,
    String dateKind,
    String dateFrom,
    String dateTo,
    String teamCode,
    String operatorCode,
    String salesManCode,
    String polCode,
    String podCode,
    String partyKind,
    String partyCode
) {}
