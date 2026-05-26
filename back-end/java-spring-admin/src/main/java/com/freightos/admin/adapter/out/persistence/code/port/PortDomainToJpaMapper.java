package com.freightos.admin.adapter.out.persistence.code.port;

import com.freightos.admin.domain.code.port.entity.Port;
import org.springframework.stereotype.Component;

@Component
public class PortDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public PortJpaEntity toNewJpa(Port domain) {
        PortJpaEntity entity = new PortJpaEntity();
        entity.setPortCode(domain.getPortCode());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setCountryCode(domain.getCountryCode());
        entity.setPortType(domain.getPortType());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. portCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(PortJpaEntity entity, Port patch) {
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setCountryCode(patch.getCountryCode());
        entity.setPortType(patch.getPortType());
        entity.setActive(patch.isActive());
    }
}
