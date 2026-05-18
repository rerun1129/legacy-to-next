package com.freightos.admin.application.auth.projection;

import com.freightos.admin.domain.user.entity.UserRole;

import java.util.List;
import java.util.Map;

public record MeProjection(
    Long id,
    String username,
    String email,
    UserRole role,
    Map<String, List<String>> attributes,
    List<String> accessibleMenus,
    List<String> accessibleButtons
) {}
