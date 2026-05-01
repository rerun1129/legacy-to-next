package com.freightos.fms.domain.common.enums;

public enum ServiceTerm {

    CY_CY("CY/CY"),
    CY_CFS("CY/CFS"),
    CFS_CFS("CFS/CFS"),
    CFS_CY("CFS/CY"),
    BULK("BULK"),
    FIOS("F.I.O.S."),
    FIFO("F.I.F.O."),
    FIBT("F.I.B.T."),
    BT_BT("BT/BT"),
    DOOR_DOOR("DOOR/DOOR"),
    CY_TK("CY/TK"),
    TK_TK("TK/TK"),
    TK_CY("TK/CY"),
    CY_DOOR("CY/DOOR"),
    BERTH_BERTH("BERTH/BERTH"),
    RO_RO("RO-RO"),
    CFS_DOOR("CFS/DOOR"),
    DOOR_CY("DOOR/CY"),
    DOOR_CFS("DOOR/CFS"),
    CY_FO("CY/FO"),
    FILO("F.I.L.O."),
    CY_RAMP("CY/RAMP"),
    DOOR_TML("DOOR/TML"),
    CY_TML("CY/TML"),
    TML_TML("TML/TML"),
    TML_CY("TML/CY"),
    TML_DOOR("TML/DOOR");

    private final String code;

    ServiceTerm(String code) { this.code = code; }

    public String getCode() { return code; }

    public static ServiceTerm fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (ServiceTerm t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new IllegalArgumentException("Unknown service term code: " + code);
    }
}
