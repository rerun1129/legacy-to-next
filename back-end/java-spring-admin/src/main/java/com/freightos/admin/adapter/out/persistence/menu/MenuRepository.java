package com.freightos.admin.adapter.out.persistence.menu;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuJpaEntity, Long>, MenuRepositoryCustom {
    boolean existsByMenuCode(String menuCode);
    boolean existsByModuleCode(String moduleCode);
    boolean existsByParentId(Long parentId);
}
