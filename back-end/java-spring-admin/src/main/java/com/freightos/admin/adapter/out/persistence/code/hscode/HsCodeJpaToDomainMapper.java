package com.freightos.admin.adapter.out.persistence.code.hscode;

import com.freightos.admin.domain.code.hscode.entity.HsCode;
import org.springframework.stereotype.Component;

@Component
public class HsCodeJpaToDomainMapper {

    public HsCode toDomain(HsCodeJpaEntity e) {
        HsCode domain = HsCode.create(e.getHsCode(), e.getName(), e.getNameEn(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
