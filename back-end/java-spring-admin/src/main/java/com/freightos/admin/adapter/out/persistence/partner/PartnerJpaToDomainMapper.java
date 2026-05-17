package com.freightos.admin.adapter.out.persistence.partner;

import com.freightos.admin.domain.partner.entity.Partner;
import org.springframework.stereotype.Component;

@Component
public class PartnerJpaToDomainMapper {

    public Partner toDomain(PartnerJpaEntity e) {
        Partner domain = Partner.create(
                e.getPartnerCode(), e.getPartnerType(), e.getName(), e.getNameEn(),
                e.getBusinessNo(), e.getRepresentative(), e.getPhone(), e.getEmail(),
                e.getAddress(), e.getMemo(), e.getActive()
        );
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
