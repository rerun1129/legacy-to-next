package com.freightos.admin.adapter.out.persistence.code.hscode;

import com.freightos.admin.domain.code.hscode.entity.HsCode;
import org.springframework.stereotype.Component;

@Component
public class HsCodeDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public HsCodeJpaEntity toNewJpa(HsCode domain) {
        HsCodeJpaEntity entity = new HsCodeJpaEntity();
        entity.setHsCode(domain.getHsCode());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. hsCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(HsCodeJpaEntity entity, HsCode patch) {
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setActive(patch.isActive());
    }
}
