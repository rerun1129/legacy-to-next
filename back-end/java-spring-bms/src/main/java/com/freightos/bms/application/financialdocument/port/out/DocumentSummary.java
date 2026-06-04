package com.freightos.bms.application.financialdocument.port.out;

/**
 * 금융 서류 편집(amend) 시 필요한 서류 요약 정보.
 * loadDocumentSummary 포트 메서드가 반환한다.
 */
public record DocumentSummary(
        Long financialDocumentId,
        String documentNo,
        String customerCode,
        String financialDocType,
        String performanceDt
) {}
