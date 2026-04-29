package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record CustomerCode(String value) {

    public CustomerCode {
        Objects.requireNonNull(value, "CustomerCode value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("CustomerCode value must not be blank");
    }

    public static CustomerCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new CustomerCode(value);
    }
}
