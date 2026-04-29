package com.freightos.fms.domain.masterbl.enums;

public enum MasterBlJobDiv {
    SEA, AIR;

    public static MasterBlJobDiv fromCode(String code) {
        if (code == null || code.isBlank()) return null;
        try {
            return MasterBlJobDiv.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
