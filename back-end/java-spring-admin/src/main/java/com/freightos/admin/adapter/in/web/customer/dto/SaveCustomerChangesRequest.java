package com.freightos.admin.adapter.in.web.customer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveCustomerChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateCustomerRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateCustomerItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
