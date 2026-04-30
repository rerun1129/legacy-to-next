package com.freightos.fms.domain.common.vo;

import com.freightos.fms.domain.housebl.enums.HandlingInfoCode;

public record HandlingInformation(HandlingInfoCode code, String description) {

    public static HandlingInformation of(HandlingInfoCode code, String description) {
        boolean codeEmpty = (code == null);
        boolean descEmpty = (description == null || description.isBlank());
        if (codeEmpty && descEmpty) return null;
        return new HandlingInformation(code, description);
    }
}
