package com.freightos.fms.application.enums.projection;

public record EnumOption(String code, String label, String description) {

    public static EnumOption fromName(Enum<?> e) {
        return new EnumOption(e.name(), e.name(), null);
    }
}
