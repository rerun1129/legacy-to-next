package com.freightos.admin.adapter.in.web.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record UpdateUserRequest(
        @Email @Size(max = 200) String email,
        @Size(min = 8, max = 100) String password,
        @NotNull Boolean active,
        @NotNull Map<String, List<String>> attributes,
        Long teamId,
        Long subscriberId
) {}
