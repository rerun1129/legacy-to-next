package com.freightos.fms.domain.common.vo;

public record AirportCode(String value) {

    public AirportCode {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("AirportCode value must not be blank");
        // IATA 2~3자 혼재 가능 — 형식 강제 없음
    }

    public static AirportCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new AirportCode(value);
    }
}
