package com.freightos.bms.adapter.in.web.financialdocument.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 운임 행 발급 요청 DTO.
 * issueType은 URL 엔드포인트(/tax, /slip)에서 결정 — body에 포함하지 않는다(Controller에서 주입).
 * issueDt: 발급일 yyyyMMdd.
 */
public record IssueFreightLineRequest(
        @NotBlank @Size(min = 8, max = 8) String issueDt,
        @NotEmpty List<Long> lineIds
) {}
