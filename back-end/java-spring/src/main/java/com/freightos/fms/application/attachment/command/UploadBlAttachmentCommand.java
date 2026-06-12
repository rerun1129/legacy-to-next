package com.freightos.fms.application.attachment.command;

import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;

import java.io.InputStream;

/**
 * B/L 첨부파일 업로드 커맨드.
 * content InputStream은 호출자가 close 책임을 지며,
 * Service가 store 완료 후 사용한다.
 */
public record UploadBlAttachmentCommand(
        AttachmentBlKind blKind,
        Long blId,
        String originalFilename,
        String contentType,
        long size,
        InputStream content,
        String uploadedBy
) {
}
