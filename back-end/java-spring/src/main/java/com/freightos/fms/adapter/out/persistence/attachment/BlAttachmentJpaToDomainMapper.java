package com.freightos.fms.adapter.out.persistence.attachment;

import com.freightos.fms.adapter.out.persistence.attachment.entity.BlAttachmentJpaEntity;
import com.freightos.fms.domain.attachment.entity.BlAttachment;
import org.springframework.stereotype.Component;

/**
 * JPA 엔티티 → 도메인 엔티티 변환 (단방향).
 */
@Component
public class BlAttachmentJpaToDomainMapper {

    public BlAttachment toDomain(BlAttachmentJpaEntity jpa) {
        return new BlAttachment(
                jpa.getBlAttachmentId(),
                jpa.getBlKind(),
                jpa.getBlId(),
                jpa.getOriginalFilename(),
                jpa.getStorageKey(),
                jpa.getContentType(),
                jpa.getFileSize(),
                jpa.getUploadedBy(),
                jpa.getCreatedAt()
        );
    }
}
