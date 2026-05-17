package com.freightos.admin.adapter.out.persistence.faqcategory;

import com.freightos.admin.domain.faqcategory.entity.FaqCategory;
import org.springframework.stereotype.Component;

@Component
public class FaqCategoryDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public FaqCategoryJpaEntity toNewJpa(FaqCategory domain) {
        FaqCategoryJpaEntity entity = new FaqCategoryJpaEntity();
        entity.setName(domain.getName());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /** 수정 가능한 필드만 적용. */
    public void applyUpdateFields(FaqCategoryJpaEntity entity, FaqCategory patch) {
        entity.setName(patch.getName());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.isActive());
    }
}
