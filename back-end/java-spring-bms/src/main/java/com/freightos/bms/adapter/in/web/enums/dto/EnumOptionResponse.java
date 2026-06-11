package com.freightos.bms.adapter.in.web.enums.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.freightos.bms.application.enums.projection.EnumOption;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnumOptionResponse(String code, String label, String description, String labelKo) {

    public EnumOptionResponse(String code, String label, String description) {
        this(code, label, description, null);
    }

    public static EnumOptionResponse from(EnumOption option) {
        return new EnumOptionResponse(option.code(), option.label(), option.description(), option.labelKo());
    }
}
