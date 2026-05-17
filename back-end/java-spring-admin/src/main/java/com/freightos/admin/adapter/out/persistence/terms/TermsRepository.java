package com.freightos.admin.adapter.out.persistence.terms;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TermsRepository extends JpaRepository<TermsJpaEntity, Long>, TermsRepositoryCustom {
}
