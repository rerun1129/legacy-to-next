package com.freightos.bms.domain.financialdocument.enums;

/**
 * 금융 서류 상태. DB 저장값 = enum.name(). 상태 전이 규칙 §6.12.
 */
public enum DocumentStatus {
    CREATED,
    GROUPED,
    TAX,
    SLIP,
    CLEAR
}
