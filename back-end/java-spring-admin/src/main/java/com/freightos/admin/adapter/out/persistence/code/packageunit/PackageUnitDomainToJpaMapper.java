package com.freightos.admin.adapter.out.persistence.code.packageunit;

import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;
import org.springframework.stereotype.Component;

@Component
public class PackageUnitDomainToJpaMapper {

    /** 신규 저장용 엔티티 생성. id는 null — JPA가 채운다. */
    public PackageUnitJpaEntity toNewJpa(PackageUnit domain) {
        PackageUnitJpaEntity entity = new PackageUnitJpaEntity();
        entity.setPackageCode(domain.getPackageCode());
        entity.setName(domain.getName());
        entity.setNameEn(domain.getNameEn());
        entity.setActive(domain.isActive());
        entity.setDeletedAt(domain.getDeletedAt());
        return entity;
    }

    /**
     * 갱신 가능한 필드만 적용. packageCode는 불변이므로 건드리지 않는다.
     */
    public void applyUpdateFields(PackageUnitJpaEntity entity, PackageUnit patch) {
        entity.setName(patch.getName());
        entity.setNameEn(patch.getNameEn());
        entity.setActive(patch.isActive());
    }
}
