package com.freightos.fms.domain.housebl.enums;

public enum ContainerType {

    T20GP("20GP", "20` 8` General purpose container"),
    T20FR("20FR", "20' 8' Flat-rack container"),
    T20OT("20OT", "20' 8' Open-top container"),
    T20RF("20RF", "20' 8' Reefer container"),
    T20TC("20TC", "20' 8' TANK CONTAINER"),
    T20HT("20HT", "20' 8' Hanger container"),
    T20GH("20GH", "20' GARMENT ON HANGER"),
    T20RH("20RH", "20' REEFER"),
    T20HZ("20HZ", "20' HZ Container"),
    T20HQ("20HQ", "20'8' HIGH-QUBIC CONTAINER"),
    T22GP("22GP", "20` 8` General purpose container"),
    T22RE("22RE", "20` 8` Termal container"),
    T22UT("22UT", "20` 8` Open-top container"),
    T22PL("22PL", "20` 8` Platform Container"),
    F40GP("40GP", "40' 8' General purpose container"),
    F40FR("40FR", "40' 8' Flat-rack container"),
    F40OT("40OT", "40' 8' Open-top container"),
    F40RF("40RF", "40' 8' Reefer container"),
    F40TC("40TC", "40' 8' TANK CONTAINER"),
    F40HT("40HT", "40' 8' Hanger container"),
    F40GH("40GH", "40' GARMENT ON HANGER"),
    F40RH("40RH", "40' 8' Reefer container"),
    F40HQ("40HQ", "40` 8` HIGH-QUBIC container"),
    F40SR("40SR", "40' SUPER RACK"),
    F40HS("40HS", "40'HC SUPER RACK CONTAINER"),
    F40NR("40NR", "40` Non-Operational Refrigerated Containers"),
    F40FH("40FH", "40'HC FLAT RACK CONTAINER"),
    F42GP("42GP", "40` 8` General purpose container"),
    F42RE("42RE", "40` 8` Termal container"),
    F42UT("42UT", "40` 8` Open-top container"),
    F42PL("42PL", "40` 8` Platform Container"),
    F45GP("45GP", "40` 8` dry container"),
    F45FR("45FR", "45 FLAT-RACK CONTAINER"),
    F45RE("45RE", "40' 9' 6\" THERMAL CONTAINER"),
    F45HQ("45HQ", "40` 9` HIGH-QUBIC container"),
    F45R1("45R1", "40HQ REFRIGERATED");

    private final String code;
    private final String description;

    ContainerType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() { return code; }
    public String getDescription() { return description; }

    /**
     * 입력 문자열을 ContainerType으로 변환.
     * - 1차: enum name() 매칭 (예: "T20GP") — BE 응답·DB 저장값과 정합 (SSOT)
     * - 2차: getCode() 매칭 (예: "20GP") — 외부 시스템/legacy 호환
     */
    public static ContainerType fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        try { return ContainerType.valueOf(code); } catch (IllegalArgumentException ignored) {}
        for (ContainerType t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new IllegalArgumentException("Unknown container type code: " + code);
    }
}
