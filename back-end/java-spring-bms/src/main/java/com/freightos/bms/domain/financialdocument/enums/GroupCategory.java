package com.freightos.bms.domain.financialdocument.enums;

/**
 * 그룹 채번 카테고리. DB 저장값 = enum.name().
 * DEBIT·CREDIT 는 같은 D/C Note 그룹으로 통합되므로 DCNOTE 하나로 매핑.
 * 그룹 번호 이니셜 규칙: §6.12.
 */
public enum GroupCategory {

    INVOICE("I"),
    PAYMENT("P"),
    DCNOTE("D");

    private final String initial;

    GroupCategory(String initial) {
        this.initial = initial;
    }

    /** 그룹 번호 이니셜. 예: INVOICE → "I" */
    public String initial() {
        return initial;
    }

    /**
     * DocumentType → GroupCategory 변환.
     * INVOICE→INVOICE, PAYMENT→PAYMENT, DEBIT|CREDIT→DCNOTE.
     */
    public static GroupCategory fromDocumentType(DocumentType documentType) {
        return switch (documentType) {
            case INVOICE -> INVOICE;
            case PAYMENT -> PAYMENT;
            case DEBIT, CREDIT -> DCNOTE;
        };
    }

    /**
     * DocumentType name 문자열 → GroupCategory 변환.
     * DocumentType.fromName 조합. null/blank 이면 null 반환.
     */
    public static GroupCategory fromDocumentTypeName(String documentTypeName) {
        DocumentType documentType = DocumentType.fromName(documentTypeName);
        if (documentType == null) return null;
        return fromDocumentType(documentType);
    }
}
