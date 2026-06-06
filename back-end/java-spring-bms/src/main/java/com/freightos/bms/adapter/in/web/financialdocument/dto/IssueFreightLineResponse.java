package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.util.List;
import java.util.Map;

/**
 * 운임 행 발급 결과 응답 DTO.
 * issueNo: 채번된 발급 번호 (예: "T260600001").
 * affectedDocumentIds: 상태가 재파생된 서류 ID 목록.
 * statuses: 서류 ID → 갱신된 상태(TAX / SLIP) 맵.
 * FE zod 1:1 매핑 대상.
 */
public record IssueFreightLineResponse(
        String issueNo,
        List<Long> affectedDocumentIds,
        Map<String, String> statuses
) {}
