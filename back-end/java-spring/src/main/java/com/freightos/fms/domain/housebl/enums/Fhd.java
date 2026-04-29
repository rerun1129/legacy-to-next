package com.freightos.fms.domain.housebl.enums;

public enum Fhd {
    N("Not"),
    F("F.H.D"),
    D("To Door");

    private final String description;

    Fhd(String description) { this.description = description; }

    public String getDescription() { return description; }

    public static Fhd fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return Fhd.valueOf(code);
    }
}
