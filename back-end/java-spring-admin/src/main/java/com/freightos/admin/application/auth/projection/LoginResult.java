package com.freightos.admin.application.auth.projection;

import com.freightos.admin.domain.user.entity.AdminUser;
import com.freightos.admin.domain.user.entity.Permission;

import java.util.Set;

public record LoginResult(
    String accessToken,
    String refreshToken,
    AdminUser user,
    Set<Permission> permissions
) {}
