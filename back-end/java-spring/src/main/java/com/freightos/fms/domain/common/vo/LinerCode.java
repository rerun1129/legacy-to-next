package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record LinerCode(String value) {

    public LinerCode {
        Objects.requireNonNull(value, "LinerCode value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("LinerCode value must not be blank");
    }

    public static LinerCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new LinerCode(value);
    }
}
