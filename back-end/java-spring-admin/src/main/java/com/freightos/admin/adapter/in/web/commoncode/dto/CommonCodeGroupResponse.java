package com.freightos.admin.adapter.in.web.commoncode.dto;

public record CommonCodeGroupResponse(
        Long id,
        String groupCode,
        String sourceModule,
        String description,
        Boolean active
) {}
