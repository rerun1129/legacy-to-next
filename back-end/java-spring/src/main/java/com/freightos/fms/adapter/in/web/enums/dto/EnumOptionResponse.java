package com.freightos.fms.adapter.in.web.enums.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.freightos.fms.application.enums.projection.EnumOption;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnumOptionResponse(String code, String label, String description) {

    public static EnumOptionResponse from(EnumOption option) {
        return new EnumOptionResponse(option.code(), option.label(), option.description());
    }
}
