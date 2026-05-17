package com.freightos.admin.adapter.in.web.auth.dto;

import com.freightos.admin.domain.user.entity.UserRole;

import java.util.List;

public record MeResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        List<String> permissions
) {}
