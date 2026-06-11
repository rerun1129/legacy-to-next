package com.freightos.admin.application.commoncode.projection;

public record CommonCodeSummary(
        Long id,
        String groupCode,
        String code,
        String label,
        String labelKo,
        Integer sortOrder,
        Boolean active
) {}
