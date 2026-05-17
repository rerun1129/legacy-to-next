package com.freightos.admin.adapter.out.persistence.faqcategory;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqCategoryRepository extends JpaRepository<FaqCategoryJpaEntity, Long>, FaqCategoryRepositoryCustom {
}
