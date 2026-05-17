package com.freightos.admin.adapter.out.persistence.notice;

import com.freightos.admin.domain.notice.entity.Notice;
import org.springframework.stereotype.Component;

@Component
public class NoticeDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public NoticeJpaEntity toNewJpa(Notice domain) {
        NoticeJpaEntity entity = new NoticeJpaEntity();
        entity.setTitle(domain.getTitle());
        entity.setContent(domain.getContent());
        entity.setPinned(domain.isPinned());
        entity.setActive(domain.isActive());
        entity.setPublishedAt(domain.getPublishedAt());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /** 갱신 가능한 필드만 적용. */
    public void applyUpdateFields(NoticeJpaEntity entity, Notice patch) {
        entity.setTitle(patch.getTitle());
        entity.setContent(patch.getContent());
        entity.setPinned(patch.isPinned());
        entity.setActive(patch.isActive());
        entity.setPublishedAt(patch.getPublishedAt());
        entity.setExpiresAt(patch.getExpiresAt());
    }
}
