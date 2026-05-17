package com.freightos.admin.adapter.out.persistence.terms;

import com.freightos.admin.domain.terms.entity.Terms;
import org.springframework.stereotype.Component;

@Component
public class TermsDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public TermsJpaEntity toNewJpa(Terms domain) {
        TermsJpaEntity entity = new TermsJpaEntity();
        entity.setType(domain.getType());
        entity.setVersion(domain.getVersion());
        entity.setEffectiveAt(domain.getEffectiveAt());
        entity.setContent(domain.getContent());
        entity.setSummary(domain.getSummary());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /** 갱신 가능한 필드만 적용. type·version은 updatable=false이므로 포함하지 않는다. */
    public void applyUpdateFields(TermsJpaEntity entity, Terms patch) {
        entity.setContent(patch.getContent());
        entity.setSummary(patch.getSummary());
        entity.setEffectiveAt(patch.getEffectiveAt());
    }
}
