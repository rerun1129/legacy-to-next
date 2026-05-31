package com.freightos.admin.adapter.in.web.subscriber.dto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record SubscriberSummaryResponse(
        Long id,
        String subscriberCode,
        String name,
        String nameEn,
        String businessNo,
        String representative,
        String phone,
        String email,
        String memo,
        boolean active,
        OffsetDateTime deletedAt,
        LocalDateTime updatedAt
) {}
