package com.freightos.admin.adapter.out.persistence.faq;

import com.freightos.admin.domain.faq.entity.Faq;
import org.springframework.stereotype.Component;

@Component
public class FaqJpaToDomainMapper {

    public Faq toDomain(FaqJpaEntity e) {
        Faq domain = Faq.create(e.getFaqCategoryId(), e.getQuestion(), e.getAnswer(), e.getSortOrder(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
