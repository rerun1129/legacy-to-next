package com.freightos.fms.domain.common.vo;

public record AirportCode(String value) {

    public AirportCode {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("AirportCode value must not be blank");
        if (!value.matches("^[A-Z]{3}$")) throw new IllegalArgumentException("Invalid IATA airport code (expected 3 uppercase letters): " + value);
    }

    public static AirportCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new AirportCode(value);
    }
}
