package com.freightos.fms.domain.common.enums;

public enum Per {

    SHP("SHP", "Ship"),
    BL("BL", "B/L"),
    CNTR("CNTR", "CNTR"),
    RT("RT", "R/TON"),
    CB("CB", "CBM"),
    OT("OT", "OTH"),
    CW("CW", "C/WT"),
    GW("GW", "G/WT"),
    MIN("MIN", "MIN"),
    UNIT("UNIT", "UNIT"),
    SET("SET", "SET"),
    QTY("QTY", "QTY"),
    TRK("TRK", "Truck"),
    TRP("TRP", "Trip"),
    RM("RM", "Norm"),
    M2("M2", "M2"),
    LIT("LIT", "Lit"),
    TN("TN", "Ton");

    private final String code;
    private final String description;

    Per(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

    public static Per fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (Per p : values()) {
            if (p.code.equals(code)) return p;
        }
        throw new IllegalArgumentException("Unknown Per code: " + code);
    }
}
