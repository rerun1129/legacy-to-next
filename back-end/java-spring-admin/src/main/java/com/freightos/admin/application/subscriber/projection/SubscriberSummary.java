package com.freightos.admin.application.subscriber.projection;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public record SubscriberSummary(
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
