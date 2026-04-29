package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record CustomerCode(String value, String address) {

    public CustomerCode {
        Objects.requireNonNull(value, "CustomerCode value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("CustomerCode value must not be blank");
        // address는 nullable 허용
    }

    public static CustomerCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new CustomerCode(value, null);
    }

    public static CustomerCode of(String value, String address) {
        if (value == null || value.isBlank()) return null;
        return new CustomerCode(value, address);
    }
}
