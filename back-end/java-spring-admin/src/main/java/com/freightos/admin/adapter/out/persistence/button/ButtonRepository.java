package com.freightos.admin.adapter.out.persistence.button;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ButtonRepository extends JpaRepository<ButtonJpaEntity, Long>, ButtonRepositoryCustom {
    boolean existsByButtonCode(String buttonCode);
    boolean existsByMenuId(Long menuId);
}
