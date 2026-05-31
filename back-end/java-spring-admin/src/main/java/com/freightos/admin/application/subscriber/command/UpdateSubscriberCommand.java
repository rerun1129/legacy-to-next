package com.freightos.admin.application.subscriber.command;

public record UpdateSubscriberCommand(
        String name,
        String nameEn,
        String businessNo,
        String representative,
        String phone,
        String email,
        String memo,
        boolean active
) {}
