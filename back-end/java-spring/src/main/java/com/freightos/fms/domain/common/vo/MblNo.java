package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record MblNo(String value) {

    public MblNo {
        Objects.requireNonNull(value, "MblNo value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("MblNo value must not be blank");
    }

    public static MblNo of(String value) {
        if (value == null || value.isBlank()) return null;
        return new MblNo(value);
    }
}
