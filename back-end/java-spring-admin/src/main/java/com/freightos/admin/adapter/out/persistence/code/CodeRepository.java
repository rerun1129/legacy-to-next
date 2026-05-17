package com.freightos.admin.adapter.out.persistence.code;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodeRepository extends JpaRepository<CodeJpaEntity, Long>, JpaSpecificationExecutor<CodeJpaEntity>, CodeRepositoryCustom {
}
