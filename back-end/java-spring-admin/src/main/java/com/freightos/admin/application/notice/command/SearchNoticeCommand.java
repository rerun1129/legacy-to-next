package com.freightos.admin.application.notice.command;

public record SearchNoticeCommand(
        String title,
        Boolean pinned,
        String scope,
        Boolean publishedOnly,
        int page,
        int size
) {}
