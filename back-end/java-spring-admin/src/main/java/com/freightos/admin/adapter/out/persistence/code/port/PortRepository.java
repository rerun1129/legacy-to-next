package com.freightos.admin.adapter.out.persistence.code.port;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortRepository extends JpaRepository<PortJpaEntity, Long>, PortRepositoryCustom {
}
