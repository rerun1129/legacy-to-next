package com.freightos.admin.adapter.out.persistence.partner;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerRepository extends JpaRepository<PartnerJpaEntity, Long>, PartnerRepositoryCustom {
}
