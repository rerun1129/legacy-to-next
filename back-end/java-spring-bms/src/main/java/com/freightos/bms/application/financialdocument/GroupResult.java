package com.freightos.bms.application.financialdocument;

import java.util.List;

/**
 * 그룹 부여/해제 결과.
 * groupFinancialNo: 부여/합류된 그룹 번호. 전원 해제만 했으면 null.
 * groupedDocumentIds: 이번에 그룹에 포함된 서류 ID 목록.
 * ungroupedDocumentIds: 이번에 해제된 서류 ID 목록.
 */
public record GroupResult(
        String groupFinancialNo,
        List<Long> groupedDocumentIds,
        List<Long> ungroupedDocumentIds
) {}
