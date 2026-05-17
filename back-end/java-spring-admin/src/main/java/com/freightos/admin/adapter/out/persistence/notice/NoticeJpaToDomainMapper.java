package com.freightos.admin.adapter.out.persistence.notice;

import com.freightos.admin.domain.notice.entity.Notice;
import org.springframework.stereotype.Component;

@Component
public class NoticeJpaToDomainMapper {

    public Notice toDomain(NoticeJpaEntity e) {
        Notice domain = Notice.create(e.getTitle(), e.getContent(), e.getPinned(), e.getActive(), null, null);
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignPublishedAt(e.getPublishedAt());
        domain.assignExpiresAt(e.getExpiresAt());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
