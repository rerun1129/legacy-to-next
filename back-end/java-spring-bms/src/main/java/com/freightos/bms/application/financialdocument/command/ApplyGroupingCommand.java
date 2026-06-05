package com.freightos.bms.application.financialdocument.command;

import java.util.List;

/**
 * 그룹 부여/해제 커맨드.
 * groupedDocumentIds: 최종 그룹에 포함될 서류 ID 목록(모달 우측).
 * scopeDocumentIds: 모달이 다룬 전체 서류 ID 목록(좌+우). null이면 grouped와 동일하게 취급.
 */
public record ApplyGroupingCommand(
        List<Long> groupedDocumentIds,
        List<Long> scopeDocumentIds
) {}
