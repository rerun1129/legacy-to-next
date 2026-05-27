package com.freightos.admin.adapter.out.persistence.code.carrier;

import com.freightos.admin.domain.code.carrier.entity.Carrier;
import org.springframework.stereotype.Component;

@Component
public class CarrierDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public CarrierJpaEntity toNewJpa(Carrier domain) {
        CarrierJpaEntity entity = new CarrierJpaEntity();
        entity.setCarrierCode(domain.getCarrierCode());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setCarrierType(domain.getCarrierType());
        entity.setCarrierAddress(domain.getCarrierAddress());
        entity.setEdiCode(domain.getEdiCode());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. carrierCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(CarrierJpaEntity entity, Carrier patch) {
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setCarrierType(patch.getCarrierType());
        entity.setCarrierAddress(patch.getCarrierAddress());
        entity.setEdiCode(patch.getEdiCode());
        entity.setActive(patch.isActive());
    }
}
