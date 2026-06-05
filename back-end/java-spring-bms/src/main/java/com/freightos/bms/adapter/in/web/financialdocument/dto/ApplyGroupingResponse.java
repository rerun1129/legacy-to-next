package com.freightos.bms.adapter.in.web.financialdocument.dto;

import java.util.List;

/**
 * 금융 서류 그룹 부여/해제 응답 DTO.
 * groupFinancialNo: 부여/합류된 그룹 번호. 전원 해제만 했으면 null.
 */
public record ApplyGroupingResponse(
        String groupFinancialNo,
        List<Long> groupedDocumentIds,
        List<Long> ungroupedDocumentIds
) {}
