package com.freightos.bms.application.financialdocument;

/**
 * 금융 서류 편집(amend) 결과.
 * deleted=true이면 모든 라인 제거로 서류가 자동 삭제된 경우.
 */
public record AmendResult(
        Long financialDocumentId,
        String documentNo,
        boolean deleted
) {}
