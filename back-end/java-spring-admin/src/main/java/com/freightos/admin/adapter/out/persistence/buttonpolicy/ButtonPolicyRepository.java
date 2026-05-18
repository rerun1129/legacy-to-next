package com.freightos.admin.adapter.out.persistence.buttonpolicy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ButtonPolicyRepository extends JpaRepository<ButtonPolicyJpaEntity, Long>, ButtonPolicyRepositoryCustom {
    boolean existsByButtonIdAndAttributeKeyAndRequiredValue(Long buttonId, String attributeKey, String requiredValue);
    boolean existsByButtonId(Long buttonId);
    boolean existsByAttributeKey(String attributeKey);
}
