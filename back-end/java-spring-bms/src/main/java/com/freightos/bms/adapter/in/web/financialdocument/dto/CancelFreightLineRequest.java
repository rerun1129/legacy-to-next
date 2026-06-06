package com.freightos.bms.adapter.in.web.financialdocument.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 운임 행 발급 취소 요청 DTO.
 * issueType은 URL 엔드포인트(/tax/cancel, /slip/cancel)에서 결정 — body에 포함하지 않는다.
 */
public record CancelFreightLineRequest(
        @NotEmpty List<Long> lineIds
) {}
