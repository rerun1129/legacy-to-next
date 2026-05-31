package com.freightos.admin.adapter.in.web.subscriber.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchSubscriberRequest(
        String subscriberCode,
        String name,
        String scope,
        @Min(0) int page,
        @Min(1) @Max(200) int size
) {}
