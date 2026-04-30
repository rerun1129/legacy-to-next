package com.freightos.fms.domain.housebl.enums;

public enum SalesClass {
    S("Sales"), N("Nomi");

    private final String label;

    SalesClass(String label) { this.label = label; }

    public String getLabel() { return label; }

    public String getCode() { return this.name(); }

    public static SalesClass fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return SalesClass.valueOf(code);
    }
}
