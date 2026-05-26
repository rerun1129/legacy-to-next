package com.freightos.admin.adapter.out.persistence.code.port;

import com.freightos.admin.domain.code.port.entity.Port;
import org.springframework.stereotype.Component;

@Component
public class PortJpaToDomainMapper {

    public Port toDomain(PortJpaEntity e) {
        Port domain = Port.create(e.getPortCode(), e.getName(), e.getNameEn(), e.getCountryCode(), e.getPortType(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
