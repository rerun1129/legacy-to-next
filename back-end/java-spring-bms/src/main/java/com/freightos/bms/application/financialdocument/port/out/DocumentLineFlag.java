package com.freightos.bms.application.financialdocument.port.out;

/**
 * 서류별 발급 상태 집계 결과.
 * 단계 E 상태 재파생: hasTax/hasSlip 기반으로 TAX/SLIP 승급 여부를 판단한다.
 */
public record DocumentLineFlag(
        Long financialDocumentId,
        boolean hasTax,
        boolean hasSlip,
        String currentDocumentStatus
) {}
