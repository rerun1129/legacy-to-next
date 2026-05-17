package com.freightos.admin.adapter.in.web.user.dto;

import com.freightos.admin.domain.user.entity.Permission;
import com.freightos.admin.domain.user.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateUserRequest(
        @Email @Size(max = 200) String email,
        @Size(min = 8, max = 100) String password,
        @NotNull UserRole role,
        @NotNull Boolean active,
        @NotNull Set<Permission> permissions
) {}
