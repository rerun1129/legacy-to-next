package com.freightos.fms.adapter.out.persistence.attachment;

import com.freightos.fms.adapter.out.persistence.attachment.entity.BlAttachmentJpaEntity;
import com.freightos.fms.domain.attachment.entity.BlAttachment;
import org.springframework.stereotype.Component;

/**
 * 도메인 엔티티 → JPA 엔티티 변환 (단방향, 신규 저장 전용).
 */
@Component
public class BlAttachmentDomainToJpaMapper {

    public BlAttachmentJpaEntity toJpa(BlAttachment domain) {
        BlAttachmentJpaEntity jpa = new BlAttachmentJpaEntity();
        jpa.setBlKind(domain.getBlKind());
        jpa.setBlId(domain.getBlId());
        jpa.setOriginalFilename(domain.getOriginalFilename());
        jpa.setStorageKey(domain.getStorageKey());
        jpa.setContentType(domain.getContentType());
        jpa.setFileSize(domain.getFileSize());
        jpa.setUploadedBy(domain.getUploadedBy());
        return jpa;
    }
}
