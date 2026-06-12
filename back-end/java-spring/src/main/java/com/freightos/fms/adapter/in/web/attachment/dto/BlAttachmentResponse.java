package com.freightos.fms.adapter.in.web.attachment.dto;

import java.time.LocalDateTime;

public record BlAttachmentResponse(
        Long id,
        String blKind,
        Long blId,
        String originalFilename,
        String contentType,
        long fileSize,
        String uploadedBy,
        LocalDateTime createdAt
) {
}
