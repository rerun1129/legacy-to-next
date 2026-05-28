package com.freightos.admin.adapter.out.persistence.menupolicy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MenuPolicyRepository extends JpaRepository<MenuPolicyJpaEntity, Long> {
    boolean existsByMenuId(Long menuId);
    boolean existsByAttributeKey(String attributeKey);

    /**
     * active 메뉴 전체를 LEFT JOIN 으로 정책과 함께 조회.
     * 정책이 없는 메뉴도 row 가 1개 포함되며, attributeKey/requiredValue 는 null 이다.
     */
    @Query("""
            SELECT m.id        AS menuId,
                   m.menuCode  AS menuCode,
                   p.attributeKey  AS attributeKey,
                   p.requiredValue AS requiredValue
            FROM MenuJpaEntity m
            LEFT JOIN MenuPolicyJpaEntity p ON p.menuId = m.id
            WHERE m.active = true
            """)
    List<MenuPolicyEvalProjection> findAllActiveMenusWithPolicies();

    /** 특정 모듈의 active 메뉴에 설정된 attributeKey 목록 (중복 제거, module 키 제외). */
    @Query("""
            SELECT DISTINCT mp.attributeKey
            FROM MenuPolicyJpaEntity mp
            JOIN MenuJpaEntity m ON mp.menuId = m.id
            WHERE m.moduleCode = :moduleCode
              AND m.active = true
              AND mp.attributeKey != 'module'
            """)
    List<String> findDistinctAttributeKeysByModuleCode(@Param("moduleCode") String moduleCode);
}
