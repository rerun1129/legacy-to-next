package com.freightos.bms.application.financialdocument.command;

import java.util.List;

/**
 * 금융 서류 편집(amend) 명령. Adapter(in) → Application 경계 전달 객체.
 * finalLineIds는 빈 리스트 허용 — 빈 리스트면 서류 자동 삭제.
 * 헤더 4필드는 null 허용 — CREATED 상태 서류에만 갱신되며, null이면 갱신하지 않는다.
 */
public record AmendDocumentCommand(
        Long financialDocumentId,
        String blType,
        String blId,
        String freightType,
        List<Long> finalLineIds,
        String documentDt,
        String performanceDt,
        String teamCode,
        String operator
) {}
