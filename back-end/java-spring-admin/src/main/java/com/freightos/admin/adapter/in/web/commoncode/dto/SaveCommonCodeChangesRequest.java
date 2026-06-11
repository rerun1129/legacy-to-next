package com.freightos.admin.adapter.in.web.commoncode.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveCommonCodeChangesRequest(
        @NotBlank @Size(max = 80) String groupCode,
        @Valid @Size(max = 200) List<@Valid CreateCommonCodeItem> creates,
        @Valid @Size(max = 200) List<@Valid UpdateCommonCodeItem> updates
) {
    public record CreateCommonCodeItem(
            @NotBlank @Size(max = 80) String code,
            @NotBlank @Size(max = 200) String label,
            @Size(max = 200) String labelKo,
            @NotNull @Min(0) Integer sortOrder,
            @NotNull Boolean active
    ) {}

    public record UpdateCommonCodeItem(
            @NotNull Long id,
            @NotBlank @Size(max = 200) String label,
            @Size(max = 200) String labelKo,
            @NotNull @Min(0) Integer sortOrder,
            @NotNull Boolean active
    ) {}
}
