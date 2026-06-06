package com.freightos.bms.application.financialdocument;

import java.util.List;
import java.util.Map;

/**
 * 운임 행 발급 취소 결과.
 * affectedDocumentIds: 상태가 재파생(강등)된 서류 ID 목록.
 * statusByDocumentId: 서류 ID → 갱신된 상태(CREATED / GROUPED) 맵.
 */
public record CancelFreightLineResult(
        List<Long> affectedDocumentIds,
        Map<Long, String> statusByDocumentId
) {}
