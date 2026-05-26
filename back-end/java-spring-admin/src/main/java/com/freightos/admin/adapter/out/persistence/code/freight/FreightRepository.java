package com.freightos.admin.adapter.out.persistence.code.freight;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FreightRepository extends JpaRepository<FreightJpaEntity, Long>, FreightRepositoryCustom {
}
