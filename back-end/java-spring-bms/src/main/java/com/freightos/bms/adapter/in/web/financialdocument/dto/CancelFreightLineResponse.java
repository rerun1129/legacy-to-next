package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.util.List;
import java.util.Map;

/**
 * 운임 행 발급 취소 결과 응답 DTO.
 * affectedDocumentIds: 상태가 재파생(강등)된 서류 ID 목록.
 * statuses: 서류 ID → 갱신된 상태(CREATED / GROUPED) 맵.
 * FE zod 1:1 매핑 대상.
 */
public record CancelFreightLineResponse(
        List<Long> affectedDocumentIds,
        Map<String, String> statuses
) {}
