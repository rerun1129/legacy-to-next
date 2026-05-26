package com.freightos.admin.adapter.out.persistence.buttonpolicy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ButtonPolicyRepository extends JpaRepository<ButtonPolicyJpaEntity, Long>, ButtonPolicyRepositoryCustom {
    boolean existsByButtonIdAndAttributeKeyAndRequiredValue(Long buttonId, String attributeKey, String requiredValue);
    boolean existsByButtonId(Long buttonId);
    boolean existsByAttributeKey(String attributeKey);

    /**
     * active 버튼 전체를 LEFT JOIN 으로 정책과 함께 조회.
     * 정책이 없는 버튼도 row 가 1개 포함되며, attributeKey/requiredValue 는 null 이다.
     */
    @Query("""
            SELECT b.id         AS buttonId,
                   b.buttonCode AS buttonCode,
                   b.label      AS label,
                   p.attributeKey  AS attributeKey,
                   p.requiredValue AS requiredValue
            FROM ButtonJpaEntity b
            LEFT JOIN ButtonPolicyJpaEntity p ON p.buttonId = b.id
            WHERE b.active = true
            """)
    List<ButtonPolicyEvalProjection> findAllActiveButtonsWithPolicies();
}
