package com.freightos.admin.adapter.out.persistence.faq;

import com.freightos.admin.domain.faq.entity.Faq;
import org.springframework.stereotype.Component;

@Component
public class FaqDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public FaqJpaEntity toNewJpa(Faq domain) {
        FaqJpaEntity entity = new FaqJpaEntity();
        entity.setFaqCategoryId(domain.getFaqCategoryId());
        entity.setQuestion(domain.getQuestion());
        entity.setAnswer(domain.getAnswer());
        entity.setSortOrder(domain.getSortOrder());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /** 수정 가능한 필드만 적용. faqCategoryId 변경 허용(카테고리 이동). */
    public void applyUpdateFields(FaqJpaEntity entity, Faq patch) {
        entity.setFaqCategoryId(patch.getFaqCategoryId());
        entity.setQuestion(patch.getQuestion());
        entity.setAnswer(patch.getAnswer());
        entity.setSortOrder(patch.getSortOrder());
        entity.setActive(patch.isActive());
    }
}
