package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record EmployeeCode(String value) {

    public EmployeeCode {
        Objects.requireNonNull(value, "EmployeeCode value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("EmployeeCode value must not be blank");
    }

    public static EmployeeCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new EmployeeCode(value);
    }
}
