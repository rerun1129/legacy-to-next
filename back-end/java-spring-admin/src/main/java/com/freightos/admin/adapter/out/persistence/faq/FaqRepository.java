package com.freightos.admin.adapter.out.persistence.faq;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqRepository extends JpaRepository<FaqJpaEntity, Long>, FaqRepositoryCustom {
}
