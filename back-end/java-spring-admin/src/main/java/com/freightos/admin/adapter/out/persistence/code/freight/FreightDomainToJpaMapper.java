package com.freightos.admin.adapter.out.persistence.code.freight;

import com.freightos.admin.domain.code.freight.entity.Freight;
import org.springframework.stereotype.Component;

@Component
public class FreightDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public FreightJpaEntity toNewJpa(Freight domain) {
        FreightJpaEntity entity = new FreightJpaEntity();
        entity.setFreightCode(domain.getFreightCode());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setDescription(domain.getDescription());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. freightCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(FreightJpaEntity entity, Freight patch) {
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setDescription(patch.getDescription());
        entity.setActive(patch.isActive());
    }
}
