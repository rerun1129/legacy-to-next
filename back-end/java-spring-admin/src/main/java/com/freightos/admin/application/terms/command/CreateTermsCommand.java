package com.freightos.admin.application.terms.command;

import java.time.LocalDateTime;

public record CreateTermsCommand(
        String type,
        int version,
        LocalDateTime effectiveAt,
        String content,
        String summary
) {}
