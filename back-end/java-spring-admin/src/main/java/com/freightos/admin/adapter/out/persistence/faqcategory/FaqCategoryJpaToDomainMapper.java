package com.freightos.admin.adapter.out.persistence.faqcategory;

import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
import org.springframework.stereotype.Component;

@Component
public class FaqCategoryJpaToDomainMapper {

    public FaqCategory toDomain(FaqCategoryJpaEntity e) {
        FaqCategory domain = FaqCategory.create(e.getName(), e.getSortOrder(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
