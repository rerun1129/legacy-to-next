package com.freightos.admin.adapter.in.web.auth.dto;

import com.freightos.admin.domain.user.entity.UserRole;

import java.util.List;
import java.util.Map;

public record MeResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        Map<String, List<String>> attributes,
        List<String> accessibleMenus,
        List<String> accessibleButtons
) {}
