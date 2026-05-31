package com.freightos.admin.adapter.out.persistence.subscription;

import com.freightos.admin.application.subscription.projection.SubscriptionSummary;
import com.freightos.admin.domain.subscription.entity.Subscription;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionJpaToDomainMapper {

    public Subscription toDomain(SubscriptionJpaEntity e) {
        Subscription domain = Subscription.create(e.getSubscriberId(), e.getModuleCode(),
                e.getStartDate(), e.getEndDate(), e.getActive());
        domain.assignIdentity(e.getSubscriptionId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        return domain;
    }

    public SubscriptionSummary toSummary(SubscriptionJpaEntity e) {
        return new SubscriptionSummary(e.getSubscriptionId(), e.getSubscriberId(), e.getModuleCode(),
                e.getStartDate(), e.getEndDate(), e.getActive(), e.getCreatedAt(), e.getUpdatedAt());
    }
}
