package com.freightos.admin.adapter.out.persistence.menu;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface MenuRepository extends JpaRepository<MenuJpaEntity, Long>, MenuRepositoryCustom {
    boolean existsByMenuCode(String menuCode);
    boolean existsByModuleCode(String moduleCode);
    boolean existsByParentId(Long parentId);
    List<MenuJpaEntity> findAllByActiveTrueAndModuleCodeAndMenuCodeIn(String moduleCode, Set<String> menuCodes);
    List<MenuJpaEntity> findAllByActiveTrueAndIdIn(Set<Long> ids);
}
