package com.freightos.admin.adapter.out.persistence.code.packageunit;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PackageUnitRepository extends JpaRepository<PackageUnitJpaEntity, Long>, PackageUnitRepositoryCustom {
}
