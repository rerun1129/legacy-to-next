package com.freightos.admin.adapter.out.persistence.subscriber;

import com.freightos.admin.domain.subscriber.entity.Subscriber;
import org.springframework.stereotype.Component;

@Component
public class SubscriberDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. subscriberId는 null — JPA가 채운다. */
    public SubscriberJpaEntity toNewJpa(Subscriber domain) {
        SubscriberJpaEntity entity = new SubscriberJpaEntity();
        entity.setSubscriberCode(domain.getSubscriberCode());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setBusinessNo(domain.getBusinessNo());
        entity.setRepresentative(domain.getRepresentative());
        entity.setPhone(domain.getPhone());
        entity.setEmail(domain.getEmail());
        entity.setMemo(domain.getMemo());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. subscriberCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(SubscriberJpaEntity entity, Subscriber patch) {
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setBusinessNo(patch.getBusinessNo());
        entity.setRepresentative(patch.getRepresentative());
        entity.setPhone(patch.getPhone());
        entity.setEmail(patch.getEmail());
        entity.setMemo(patch.getMemo());
        entity.setActive(patch.isActive());
    }
}
