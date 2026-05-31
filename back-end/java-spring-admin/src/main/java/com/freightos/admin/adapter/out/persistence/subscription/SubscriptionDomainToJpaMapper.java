package com.freightos.admin.adapter.out.persistence.subscription;

import com.freightos.admin.domain.subscription.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. subscriptionId는 null — JPA가 채운다. */
    public SubscriptionJpaEntity toNewJpa(Subscription domain) {
        SubscriptionJpaEntity entity = new SubscriptionJpaEntity();
        entity.setSubscriberId(domain.getSubscriberId());
        entity.setModuleCode(domain.getModuleCode());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setActive(domain.isActive());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. subscriberId, moduleCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(SubscriptionJpaEntity entity, Subscription patch) {
        entity.setStartDate(patch.getStartDate());
        entity.setEndDate(patch.getEndDate());
        entity.setActive(patch.isActive());
    }
}
