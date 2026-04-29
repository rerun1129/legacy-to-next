package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record BlNumber(String value) {

    public BlNumber {
        Objects.requireNonNull(value, "BlNumber value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("BlNumber value must not be blank");
    }

    public static BlNumber of(String value) {
        if (value == null || value.isBlank()) return null;
        return new BlNumber(value);
    }
}
