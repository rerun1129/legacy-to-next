package com.freightos.admin.application.commoncode.projection;

public record CommonCodeGroupSummary(
        Long id,
        String groupCode,
        String sourceModule,
        String description,
        Boolean active
) {}
