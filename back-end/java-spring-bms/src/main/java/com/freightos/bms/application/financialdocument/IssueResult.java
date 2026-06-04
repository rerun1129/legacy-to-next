package com.freightos.bms.application.financialdocument;

/**
 * 금융 서류 발행 결과. 컨트롤러에 노출되는 최소 정보만 포함.
 * 도메인 객체를 컨트롤러에 반환하지 않기 위한 경계 VO.
 */
public record IssueResult(Long financialDocumentId, String documentNo) {}
