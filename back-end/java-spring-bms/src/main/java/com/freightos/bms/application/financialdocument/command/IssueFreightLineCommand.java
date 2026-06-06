package com.freightos.bms.application.financialdocument.command;

import java.util.List;

/**
 * 운임 행 발급 커맨드.
 * issueType: "TAX" 또는 "SLIP" (domain enum은 Service에서 변환).
 * issueDt: yyyyMMdd 형식 발급일.
 * lineIds: 발급 대상 운임 행 ID 목록.
 */
public record IssueFreightLineCommand(
        String issueType,
        String issueDt,
        List<Long> lineIds
) {}
