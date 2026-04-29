package com.freightos.fms.domain.common.vo;

public record CurrencyCode(String value) {

    public CurrencyCode {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("CurrencyCode value must not be blank");
        if (!value.matches("^[A-Z]{3}$")) throw new IllegalArgumentException("Invalid ISO 4217 currency code (expected 3 uppercase letters): " + value);
    }

    public static CurrencyCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new CurrencyCode(value);
    }
}
