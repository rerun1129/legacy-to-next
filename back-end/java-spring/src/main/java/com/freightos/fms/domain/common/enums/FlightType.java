package com.freightos.fms.domain.common.enums;

public enum FlightType {
    P("Passenger", "여객"),
    C("Cargo", "화물");

    private final String description;
    private final String labelKo;

    FlightType(String description, String labelKo) {
        this.description = description;
        this.labelKo = labelKo;
    }

    public String getDescription() { return description; }
    public String getLabelKo() { return labelKo; }

    public static FlightType fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        return FlightType.valueOf(code);
    }
}
