package com.freightos.admin.adapter.out.persistence.code.packageunit;

import com.freightos.admin.domain.code.packageunit.entity.PackageUnit;
import org.springframework.stereotype.Component;

@Component
public class PackageUnitJpaToDomainMapper {

    public PackageUnit toDomain(PackageUnitJpaEntity e) {
        PackageUnit domain = PackageUnit.create(e.getPackageCode(), e.getName(), e.getNameEn(), e.getActive());
        domain.assignIdentity(e.getId(), e.getCreatedAt(), e.getUpdatedAt(), e.getCreatedBy(), e.getUpdatedBy());
        domain.assignDeletedAt(e.getDeletedAt());
        return domain;
    }
}
