package com.freightos.admin.application.team.projection;

public record TeamSummary(
        Long id,
        String teamCode,
        String name,
        Integer sortOrder,
        Boolean active
) {}
