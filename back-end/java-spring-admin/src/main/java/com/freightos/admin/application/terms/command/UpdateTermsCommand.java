package com.freightos.admin.application.terms.command;

import java.time.LocalDateTime;

public record UpdateTermsCommand(
        String content,
        String summary,
        LocalDateTime effectiveAt
) {}
