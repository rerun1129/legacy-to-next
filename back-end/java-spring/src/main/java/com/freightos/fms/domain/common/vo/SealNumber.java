package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record SealNumber(String value) {

    public SealNumber {
        Objects.requireNonNull(value, "SealNumber value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("SealNumber value must not be blank");
    }

    public static SealNumber of(String value) {
        if (value == null || value.isBlank()) return null;
        return new SealNumber(value);
    }
}
