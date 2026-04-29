package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record ContainerNumber(String value) {

    public ContainerNumber {
        Objects.requireNonNull(value, "ContainerNumber value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("ContainerNumber value must not be blank");
        if (!value.matches("^[A-Z]{4}\\d{7}$")) throw new IllegalArgumentException("Invalid container number format (expected AAAA1234567): " + value);
    }

    public static ContainerNumber of(String value) {
        if (value == null || value.isBlank()) return null;
        return new ContainerNumber(value);
    }
}
