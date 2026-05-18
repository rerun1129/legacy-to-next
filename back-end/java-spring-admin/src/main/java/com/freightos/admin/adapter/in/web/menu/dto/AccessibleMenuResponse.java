package com.freightos.admin.adapter.in.web.menu.dto;

public record AccessibleMenuResponse(
        Long id,
        String menuCode,
        Long parentId,
        String path,
        String label,
        String labelEn,
        String icon,
        Integer sortOrder,
        String moduleCode
) {}
