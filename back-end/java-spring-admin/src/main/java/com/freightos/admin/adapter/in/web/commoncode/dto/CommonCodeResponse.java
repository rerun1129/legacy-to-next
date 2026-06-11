package com.freightos.admin.adapter.in.web.commoncode.dto;

public record CommonCodeResponse(
        Long id,
        String groupCode,
        String code,
        String label,
        String labelKo,
        Integer sortOrder,
        Boolean active
) {}
