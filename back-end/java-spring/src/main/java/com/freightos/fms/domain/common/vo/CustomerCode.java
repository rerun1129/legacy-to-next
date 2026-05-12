package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record CustomerCode(String value, String address) {

    /**
     * Canonical constructor.
     *
     * 기존 invariant 보존: value 단독 null/blank이면 NPE/IAE.
     * 신규 허용: value가 null/blank이어도 address가 있으면 VO 생성(legacy free-text address 지원).
     *
     * 결과 정규화: blank value/address는 null로 통일하여 .value()/.address() 반환 일관성 유지.
     */
    public CustomerCode {
        boolean addressBlank = address == null || address.isBlank();

        if (value == null && addressBlank) {
            // 기존 호환: value=null + address 없음 → NPE
            Objects.requireNonNull(value, "CustomerCode value must not be null");
        }
        if (value != null && value.isBlank() && addressBlank) {
            // 기존 호환: value=blank + address 없음 → IAE
            throw new IllegalArgumentException("CustomerCode value must not be blank");
        }

        // blank → null 정규화
        if (value   != null && value.isBlank())   value   = null;
        if (addressBlank)                          address = null;
    }

    public static CustomerCode of(String value) {
        if (value == null || value.isBlank()) return null;
        return new CustomerCode(value, null);
    }

    /**
     * value가 비어도 address가 있으면 VO 생성(legacy free-text address 지원).
     * 둘 다 비어있으면 null 반환.
     */
    public static CustomerCode of(String value, String address) {
        boolean valueEmpty   = value   == null || value.isBlank();
        boolean addressEmpty = address == null || address.isBlank();
        if (valueEmpty && addressEmpty) return null;
        return new CustomerCode(value, address);
    }
}
