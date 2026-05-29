package com.freightos.admin.adapter.in.web.codemaster.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveCodeMasterChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateCodeMasterRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateCodeMasterItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
