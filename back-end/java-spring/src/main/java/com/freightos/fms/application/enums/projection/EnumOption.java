package com.freightos.fms.application.enums.projection;

public record EnumOption(String code, String label, String description, String labelKo) {

    public EnumOption(String code, String label, String description) {
        this(code, label, description, null);
    }

    public static EnumOption fromName(Enum<?> e) {
        return new EnumOption(e.name(), e.name(), null, null);
    }
}
