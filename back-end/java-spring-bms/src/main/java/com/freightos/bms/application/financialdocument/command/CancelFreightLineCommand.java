package com.freightos.bms.application.financialdocument.command;

import java.util.List;

/**
 * 운임 행 발급 취소 커맨드.
 * issueType: "TAX" 또는 "SLIP" (domain enum은 Service에서 변환).
 * lineIds: 취소 대상 운임 행 ID 목록.
 */
public record CancelFreightLineCommand(
        String issueType,
        List<Long> lineIds
) {}
