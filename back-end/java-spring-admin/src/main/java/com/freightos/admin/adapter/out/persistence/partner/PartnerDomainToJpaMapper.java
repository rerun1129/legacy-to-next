package com.freightos.admin.adapter.out.persistence.partner;

import com.freightos.admin.domain.partner.entity.Partner;
import org.springframework.stereotype.Component;

@Component
public class PartnerDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public PartnerJpaEntity toNewJpa(Partner domain) {
        PartnerJpaEntity entity = new PartnerJpaEntity();
        entity.setPartnerCode(domain.getPartnerCode());
        entity.setPartnerType(domain.getPartnerType());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setBusinessNo(domain.getBusinessNo());
        entity.setRepresentative(domain.getRepresentative());
        entity.setPhone(domain.getPhone());
        entity.setEmail(domain.getEmail());
        entity.setAddress(domain.getAddress());
        entity.setMemo(domain.getMemo());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. partnerCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(PartnerJpaEntity entity, Partner patch) {
        entity.setPartnerType(patch.getPartnerType());
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setBusinessNo(patch.getBusinessNo());
        entity.setRepresentative(patch.getRepresentative());
        entity.setPhone(patch.getPhone());
        entity.setEmail(patch.getEmail());
        entity.setAddress(patch.getAddress());
        entity.setMemo(patch.getMemo());
        entity.setActive(patch.isActive());
    }
}
