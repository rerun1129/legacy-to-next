package com.freightos.admin.adapter.out.persistence.terms;

import com.freightos.admin.domain.terms.entity.Terms;
import org.springframework.stereotype.Component;

@Component
public class TermsJpaToDomainMapper {

    public Terms toDomain(TermsJpaEntity e) {
        Terms domain = Terms.create(e.getType(), e.getVersion(), null, e.getContent(), e.getSummary());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignEffectiveAt(e.getEffectiveAt());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
