package com.freightos.admin.adapter.out.persistence.codedetail;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodeDetailRepository extends JpaRepository<CodeDetailJpaEntity, Long>, JpaSpecificationExecutor<CodeDetailJpaEntity>, CodeDetailRepositoryCustom {

    long countByMasterId(Long masterId);
}
