package com.freightos.fms.domain.common.vo;

public record PortCode(String value) {

    public PortCode {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("PortCode value must not be blank");
        // UN/LOCODE 형식(5자)은 실제 데이터 확인 후 강화 예정
    }

    public static PortCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new PortCode(value);
    }
}
