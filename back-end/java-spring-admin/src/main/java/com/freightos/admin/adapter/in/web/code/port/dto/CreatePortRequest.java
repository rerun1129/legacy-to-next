package com.freightos.admin.adapter.in.web.code.port.dto;

import com.freightos.admin.domain.code.port.entity.PortType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePortRequest(
        @NotBlank @Size(max = 10) @Pattern(regexp = "^[A-Za-z0-9_-]+$") String portCode,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 200) String nameEn,
        @Size(max = 3) String countryCode,
        @NotNull PortType portType,
        @NotNull Boolean active
) {}
