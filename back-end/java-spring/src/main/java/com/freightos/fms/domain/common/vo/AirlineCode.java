package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record AirlineCode(String value) {

    public AirlineCode {
        Objects.requireNonNull(value, "AirlineCode value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("AirlineCode value must not be blank");
    }

    public static AirlineCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new AirlineCode(value);
    }
}
