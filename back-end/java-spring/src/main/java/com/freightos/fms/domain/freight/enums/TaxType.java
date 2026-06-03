package com.freightos.fms.domain.freight.enums;

/**
 * 세금 유형 — §6.7.
 * DB 저장값 = enum.name().
 */
public enum TaxType {
    TAXABLE("Taxable", "과세"),
    ZERO_RATED("Zero-rated", "영세"),
    EXEMPT("Exempt", "면세");

    private final String label;
    private final String labelKo;

    TaxType(String label, String labelKo) {
        this.label = label;
        this.labelKo = labelKo;
    }

    public String getLabel() { return label; }
    public String getLabelKo() { return labelKo; }

    /** name 문자열 → enum. null/blank 이면 null 반환. */
    public static TaxType fromName(String name) {
        if (name == null || name.isBlank()) return null;
        return TaxType.valueOf(name);
    }
}
