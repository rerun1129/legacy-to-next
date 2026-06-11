package com.freightos.bms.application.enums.projection;

public record EnumOption(String code, String label, String description, String labelKo) {

    public EnumOption(String code, String label, String description) {
        this(code, label, description, null);
    }
}
