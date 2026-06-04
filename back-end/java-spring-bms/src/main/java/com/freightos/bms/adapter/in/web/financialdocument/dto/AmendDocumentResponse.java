package com.freightos.bms.adapter.in.web.financialdocument.dto;

/**
 * 금융 서류 편집(amend) 응답 DTO.
 * deleted=true이면 모든 라인 제거로 서류가 자동 삭제된 경우 — FE가 서류 삭제 처리에 활용한다.
 */
public record AmendDocumentResponse(
        Long financialDocumentId,
        String documentNo,
        boolean deleted
) {}
