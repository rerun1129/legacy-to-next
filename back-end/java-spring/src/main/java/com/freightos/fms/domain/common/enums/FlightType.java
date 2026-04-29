package com.freightos.fms.domain.common.enums;

public enum FlightType {
    P("Passenger"),
    C("Cargo");

    private final String description;

    FlightType(String description) { this.description = description; }

    public String getDescription() { return description; }

    public static FlightType fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return FlightType.valueOf(code);
    }
}
