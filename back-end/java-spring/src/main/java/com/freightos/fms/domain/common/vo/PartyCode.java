package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record PartyCode(String value) {

    public PartyCode {
        Objects.requireNonNull(value, "PartyCode value must not be null");
        if (value.isBlank()) throw new IllegalArgumentException("PartyCode value must not be blank");
    }

    public static PartyCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new PartyCode(value);
    }
}
