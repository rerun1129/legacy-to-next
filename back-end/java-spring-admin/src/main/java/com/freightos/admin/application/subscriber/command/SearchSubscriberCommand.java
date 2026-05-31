package com.freightos.admin.application.subscriber.command;

public record SearchSubscriberCommand(
        String subscriberCode,
        String name,
        String scope,
        int page,
        int size
) {}
