package com.freightos.bms.domain.financialdocument;

import com.freightos.bms.domain.financialdocument.enums.DocumentType;

/**
 * 서류 번호 값 객체. §6.11 채번 규칙: 이니셜 + YYMM + 5자리 시퀀스.
 * 순수 VO — Spring/JPA import 없음.
 */
public record DocumentNo(String value) {

    /**
     * 서류 번호 생성.
     * 예: DocumentType.INVOICE, "2606", 1 → "I260600001"
     */
    public static DocumentNo of(DocumentType type, String yymm, int seq) {
        String formatted = type.initial() + yymm + String.format("%05d", seq);
        return new DocumentNo(formatted);
    }
}
