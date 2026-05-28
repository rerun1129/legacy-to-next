package com.freightos.admin.adapter.in.web.attributevalue.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveAttributeValueChangesRequest(
        @NotBlank @Size(max = 80) String attributeKey,
        @Valid @Size(max = 100) List<@Valid CreateAttributeValueRequest> creates,
        @Valid @Size(max = 100) List<@Valid SaveAttributeValueItem> updates,
        @Size(max = 200) List<@NotNull Long> deleteIds
) {
    public record SaveAttributeValueItem(
            @NotNull Long id,
            @NotBlank @Size(max = 200) String label,
            @Min(0) Integer sortOrder,
            @NotNull Boolean active
    ) {}
}
