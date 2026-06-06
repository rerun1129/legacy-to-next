package com.freightos.bms.application.financialdocument.port.out;

/**
 * 서류별 발급 상태 집계 결과.
 * 단계 E 상태 재파생: hasTax/hasSlip 기반으로 TAX/SLIP 승급 여부를 판단한다.
 * groupFinancialNo: 취소 시 GROUPED 상태 재파생에 사용(null 이면 CREATED).
 */
public record DocumentLineFlag(
        Long financialDocumentId,
        boolean hasTax,
        boolean hasSlip,
        String currentDocumentStatus,
        String groupFinancialNo
) {
    /** 하위호환 편의 생성자 — groupFinancialNo null로 위임. 기존 호출부 보존. */
    public DocumentLineFlag(Long financialDocumentId, boolean hasTax, boolean hasSlip, String currentDocumentStatus) {
        this(financialDocumentId, hasTax, hasSlip, currentDocumentStatus, null);
    }
}
