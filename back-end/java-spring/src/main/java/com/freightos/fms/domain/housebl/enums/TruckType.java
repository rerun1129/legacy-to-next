package com.freightos.fms.domain.housebl.enums;

import java.math.BigDecimal;

public enum TruckType {

    T12("T12",  new BigDecimal("1.2"),  new BigDecimal("3.1")),
    T25("T25",  new BigDecimal("2.5"),  new BigDecimal("4.3")),
    T35("T35",  new BigDecimal("3.5"),  new BigDecimal("4.8")),
    T50("T50",  new BigDecimal("5.0"),  new BigDecimal("5.2")),
    T80("T80",  new BigDecimal("8.0"),  new BigDecimal("7.8")),
    T100("T100", new BigDecimal("10.0"), new BigDecimal("9.6"));

    private final String code;
    private final BigDecimal tonnage;
    private final BigDecimal lengthMeter;

    TruckType(String code, BigDecimal tonnage, BigDecimal lengthMeter) {
        this.code        = code;
        this.tonnage     = tonnage;
        this.lengthMeter = lengthMeter;
    }

    public String getCode() { return code; }
    public BigDecimal getTonnage() { return tonnage; }
    public BigDecimal getLengthMeter() { return lengthMeter; }

    public static TruckType fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        for (TruckType t : values()) {
            if (t.code.equals(code)) return t;
        }
        throw new IllegalArgumentException("Unknown truck type code: " + code);
    }
}
