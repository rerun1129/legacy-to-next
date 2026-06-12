package com.freightos.fms.application.attachment.projection;

import java.io.InputStream;

/**
 * 첨부파일 다운로드 응답 — 메타데이터 + 파일 스트림.
 * 호출자는 stream을 소비한 후 close 해야 한다.
 */
public record BlAttachmentContent(
        String originalFilename,
        String contentType,
        long fileSize,
        InputStream stream
) {
}
