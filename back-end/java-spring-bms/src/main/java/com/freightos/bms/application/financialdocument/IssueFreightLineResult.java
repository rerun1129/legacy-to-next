package com.freightos.bms.application.financialdocument;

import java.util.List;
import java.util.Map;

/**
 * 운임 행 발급 결과.
 * issueNo: 채번된 발급 번호 (예: "T260600001").
 * affectedDocumentIds: 상태가 재파생된 서류 ID 목록.
 * statusByDocumentId: 서류 ID → 갱신된 상태 (TAX / SLIP) 맵.
 */
public record IssueFreightLineResult(
        String issueNo,
        List<Long> affectedDocumentIds,
        Map<Long, String> statusByDocumentId
) {}
