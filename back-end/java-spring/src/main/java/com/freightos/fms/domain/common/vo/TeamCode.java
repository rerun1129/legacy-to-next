package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record TeamCode(String value) {

    public TeamCode {
        Objects.requireNonNull(value, "TeamCode value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("TeamCode value must not be blank");
    }

    public static TeamCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new TeamCode(value);
    }
}
