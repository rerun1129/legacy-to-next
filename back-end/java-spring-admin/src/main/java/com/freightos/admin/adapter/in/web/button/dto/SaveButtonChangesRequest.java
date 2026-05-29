package com.freightos.admin.adapter.in.web.button.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveButtonChangesRequest(
        @Valid @Size(max = 100) List<@Valid CreateButtonItem> creates,
        @Valid @Size(max = 100) List<@Valid UpdateButtonItem> updates
) {
    public record CreateButtonItem(
            @NotBlank @Size(max = 80) @Pattern(regexp = "^[A-Z][A-Z0-9_]*$") String buttonCode,
            @NotNull Long menuId,
            @NotBlank @Size(max = 200) String label,
            @NotBlank String actionType,
            @Size(max = 10) String apiMethod,
            @Size(max = 200) String apiPath,
            @Min(0) Integer sortOrder,
            @NotNull Boolean active
    ) {}

    public record UpdateButtonItem(
            @NotNull Long id,
            @NotNull Long menuId,
            @NotBlank @Size(max = 200) String label,
            @NotBlank String actionType,
            @Size(max = 10) String apiMethod,
            @Size(max = 200) String apiPath,
            @Min(0) Integer sortOrder,
            @NotNull Boolean active
    ) {}
}
