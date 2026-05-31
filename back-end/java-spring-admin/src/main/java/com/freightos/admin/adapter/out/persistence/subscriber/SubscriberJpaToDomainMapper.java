package com.freightos.admin.adapter.out.persistence.subscriber;

import com.freightos.admin.domain.subscriber.entity.Subscriber;
import org.springframework.stereotype.Component;

@Component
public class SubscriberJpaToDomainMapper {

    public Subscriber toDomain(SubscriberJpaEntity e) {
        Subscriber domain = Subscriber.create(e.getSubscriberCode(), e.getName(), e.getNameEn(),
                e.getBusinessNo(), e.getRepresentative(), e.getPhone(), e.getEmail(), e.getMemo(), e.getActive());
        domain.assignIdentity(e.getSubscriberId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
