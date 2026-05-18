package com.freightos.admin.adapter.out.persistence.attributedefinition;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttributeDefinitionRepository extends JpaRepository<AttributeDefinitionJpaEntity, Long>, AttributeDefinitionRepositoryCustom {
    Optional<AttributeDefinitionJpaEntity> findByAttributeKey(String attributeKey);
    boolean existsByAttributeKey(String attributeKey);
}
