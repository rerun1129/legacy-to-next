package com.freightos.bms.application.financialdocument.command;

import java.util.List;

/**
 * 금융 서류 발행 명령. Adapter(in) → Application 경계 전달 객체.
 */
public record IssueDocumentCommand(
        String blType,
        Long blId,
        String freightType,
        List<Long> lineIds,
        String documentDt,
        String performanceDt,
        String teamCode,
        String operator
) {}
