package com.freightos.admin.adapter.in.web.subscriber.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaveSubscriberChangesRequest(
        @Valid @Size(max = 50) List<@Valid CreateSubscriberRequest> creates,
        @Valid @Size(max = 50) List<@Valid UpdateSubscriberItemRequest> updates,
        @Size(max = 100) List<@NotNull Long> deleteIds
) {}
