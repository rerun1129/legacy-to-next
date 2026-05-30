package com.freightos.admin.adapter.in.web.team.dto;

public record TeamSummaryResponse(
        Long id,
        String teamCode,
        String name,
        Integer sortOrder,
        Boolean active
) {}
