package com.freightos.fms.domain.housebl.enums;

public enum CargoType {
    NR("NR", "Normal"),
    DG("DG", "Danger"),
    KC("KC", "Keep Cool"),
    KF("KF", "Keep Frozen"),
    OS("OS", "Over Size"),
    SWTW("SWTW", "S/W,T/W"),
    NS("NS", "Non-Stackable"),
    FD("FD", "Food");

    private final String code;
    private final String description;

    CargoType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

    public static CargoType fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (CargoType c : values()) if (c.code.equals(code)) return c;
        throw new IllegalArgumentException("Unknown CargoType code: " + code);
    }
}
