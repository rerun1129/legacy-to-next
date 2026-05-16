package com.freightos.fms.domain.common.vo;

import java.util.Objects;

public record CustomerCode(String value, String address) {

    /**
     * Canonical constructor.
     *
     * 기존 invariant 보존: value 단독 null/blank이면 NPE/IAE.
     * 신규 허용: value가 null/blank이어도 address가 있으면 VO 생성(legacy free-text address 지원).
     *
     * 결과 정규화:
     *  - blank value/address는 null로 통일하여 .value()/.address() 반환 일관성 유지
     *  - non-blank value/address는 trim 적용하여 round-trip 동등성 보장 (§6.63 — DB의
     *    trailing/leading whitespace로 인해 무수정 저장 시 dirty가 잡혀 UPDATE가 발사되는 회귀 차단)
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

        // blank → null 정규화, non-blank → trim 정규화 (§6.63)
        if (value == null || value.isBlank())       value   = null;
        else                                         value   = value.trim();
        if (addressBlank)                            address = null;
        else                                         address = address.trim();
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
