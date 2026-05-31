package com.freightos.admin.adapter.in.web.subscription.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveSubscriptionChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateSubscriptionItemRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateSubscriptionItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
