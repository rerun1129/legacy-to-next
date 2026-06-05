package com.freightos.bms.adapter.in.web.financialdocument.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 금융 서류 그룹 부여/해제 요청 DTO.
 * groupedDocumentIds: 최종 그룹에 포함될 서류 ID 목록(모달 우측). null 불가.
 * scopeDocumentIds: 모달이 다룬 전체 서류 ID 목록(좌+우). null이면 grouped와 동일.
 */
public record ApplyGroupingRequest(
        @NotNull List<Long> groupedDocumentIds,
        List<Long> scopeDocumentIds
) {}
