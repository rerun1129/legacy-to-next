package com.freightos.admin.application.auth.projection;

import com.freightos.admin.common.security.AccessibleButton;
import com.freightos.admin.domain.user.entity.AdminUser;

import java.util.List;
import java.util.Map;

public record LoginResult(
    String accessToken,
    String refreshToken,
    AdminUser user,
    Map<String, List<String>> attributes,
    List<String> accessibleMenus,
    List<AccessibleButton> accessibleButtons
) {}
