package com.freightos.admin.adapter.out.persistence.code.freight;

import com.freightos.admin.domain.code.freight.entity.Freight;
import org.springframework.stereotype.Component;

@Component
public class FreightJpaToDomainMapper {

    public Freight toDomain(FreightJpaEntity e) {
        Freight domain = Freight.create(e.getFreightCode(), e.getName(), e.getNameEn(), e.getDescription(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
