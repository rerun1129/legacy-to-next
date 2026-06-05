package com.freightos.bms.application.financialdocument.port.out;

/**
 * 그룹 부여/해제 처리용 서류 스냅샷.
 * 검증·상태 승급/강등 판단에 필요한 최소 필드만 포함.
 */
public record GroupDocumentSnapshot(
        Long financialDocumentId,
        String customerCode,
        String documentType,
        String documentStatus,
        String groupFinancialNo,
        String documentDt
) {}
