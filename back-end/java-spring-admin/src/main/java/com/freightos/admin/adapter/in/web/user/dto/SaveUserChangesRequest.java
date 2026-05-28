package com.freightos.admin.adapter.in.web.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveUserChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateUserRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateUserItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
