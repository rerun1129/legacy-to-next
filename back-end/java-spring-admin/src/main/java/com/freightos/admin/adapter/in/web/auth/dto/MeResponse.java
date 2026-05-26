package com.freightos.admin.adapter.in.web.auth.dto;

import com.freightos.admin.common.security.AccessibleButton;

import java.util.List;
import java.util.Map;

public record MeResponse(
        Long id,
        String username,
        String email,
        Map<String, List<String>> attributes,
        List<String> accessibleMenus,
        List<AccessibleButton> accessibleButtons
) {}
