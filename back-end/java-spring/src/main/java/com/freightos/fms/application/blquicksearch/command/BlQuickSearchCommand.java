package com.freightos.fms.application.blquicksearch.command;

/**
 * BL 자동완성 검색 커맨드.
 * Adapter(in)에서 raw String으로 전달하여 도메인 enum 의존을 차단한다.
 * blNo: q 파라미터를 어셈블러에서 매핑한 값.
 */
public record BlQuickSearchCommand(
    String blNo,
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
    String partyCode,
    Integer limit
) {}
