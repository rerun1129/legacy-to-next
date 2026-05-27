package com.freightos.admin.adapter.out.persistence.code.carrier;

import com.freightos.admin.domain.code.carrier.entity.Carrier;
import org.springframework.stereotype.Component;

@Component
public class CarrierJpaToDomainMapper {

    public Carrier toDomain(CarrierJpaEntity e) {
        Carrier domain = Carrier.create(e.getCarrierCode(), e.getName(), e.getNameEn(), e.getCarrierType(), e.getCarrierAddress(), e.getEdiCode(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
