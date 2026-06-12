package com.freightos.fms.domain.attachment.entity;

import com.freightos.fms.domain.attachment.enums.AttachmentBlKind;

import java.time.LocalDateTime;

/**
 * B/L 첨부파일 도메인 엔티티.
 * 파일 실체는 StoragePort가 관리하며, 이 객체는 메타데이터만 보유한다.
 * 순수 도메인 객체 — Spring/Jakarta 의존 없음.
 */
public class BlAttachment {

    private final Long id;
    private final AttachmentBlKind blKind;
    private final Long blId;
    private final String originalFilename;
    private final String storageKey;
    private final String contentType;
    private final long fileSize;
    private final String uploadedBy;
    private final LocalDateTime createdAt;

    public BlAttachment(
            Long id,
            AttachmentBlKind blKind,
            Long blId,
            String originalFilename,
            String storageKey,
            String contentType,
            long fileSize,
            String uploadedBy,
            LocalDateTime createdAt) {
        this.id = id;
        this.blKind = blKind;
        this.blId = blId;
        this.originalFilename = originalFilename;
        this.storageKey = storageKey;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public AttachmentBlKind getBlKind() { return blKind; }
    public Long getBlId() { return blId; }
    public String getOriginalFilename() { return originalFilename; }
    public String getStorageKey() { return storageKey; }
    public String getContentType() { return contentType; }
    public long getFileSize() { return fileSize; }
    public String getUploadedBy() { return uploadedBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
