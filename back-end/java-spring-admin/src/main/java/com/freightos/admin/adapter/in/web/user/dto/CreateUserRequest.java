package com.freightos.admin.adapter.in.web.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record CreateUserRequest(
        @NotBlank @Size(max = 50) @Pattern(regexp = "^[a-z][a-z0-9_]*$") String username,
        @Email @Size(max = 200) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Boolean active,
        @NotNull Map<String, List<String>> attributes,
        Long teamId
) {}
