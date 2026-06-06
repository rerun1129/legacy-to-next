package com.freightos.bms.application.financialdocument.port.out;

/**
 * 운임 라인 발급 처리용 스냅샷.
 * FreightLineSnapshot과 별도 record — 발급 검증에 필요한 taxNo/slipNo를 포함한다.
 * FreightLineSnapshot에 필드 추가 금지(record_change_avoid_test_edits).
 */
public record FreightLineIssueSnapshot(
        Long freightLineId,
        String customerCode,
        String financialDocType,
        Long financialDocumentId,
        String taxNo,
        String slipNo
) {}
