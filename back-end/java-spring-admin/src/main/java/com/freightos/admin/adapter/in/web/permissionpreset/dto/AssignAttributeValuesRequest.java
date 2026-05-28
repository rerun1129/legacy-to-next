package com.freightos.admin.adapter.in.web.permissionpreset.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record AssignAttributeValuesRequest(
        @NotNull List<Long> addIds,
        @NotNull List<Long> removeIds
) {}
