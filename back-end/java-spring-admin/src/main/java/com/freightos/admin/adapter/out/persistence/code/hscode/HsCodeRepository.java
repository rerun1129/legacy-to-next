package com.freightos.admin.adapter.out.persistence.code.hscode;

import org.springframework.data.jpa.repository.JpaRepository;

public interface HsCodeRepository extends JpaRepository<HsCodeJpaEntity, Long>, HsCodeRepositoryCustom {
}
