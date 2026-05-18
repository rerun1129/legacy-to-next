package com.freightos.admin.adapter.out.persistence.menupolicy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuPolicyRepository extends JpaRepository<MenuPolicyJpaEntity, Long>, MenuPolicyRepositoryCustom {
    boolean existsByMenuIdAndAttributeKeyAndRequiredValue(Long menuId, String attributeKey, String requiredValue);
    boolean existsByMenuId(Long menuId);
    boolean existsByAttributeKey(String attributeKey);
}
