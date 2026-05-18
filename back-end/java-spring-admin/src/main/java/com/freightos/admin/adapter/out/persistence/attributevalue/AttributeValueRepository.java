package com.freightos.admin.adapter.out.persistence.attributevalue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValueJpaEntity, AttributeValueId>, AttributeValueRepositoryCustom {
    Optional<AttributeValueJpaEntity> findById(AttributeValueId id);
    boolean existsById(AttributeValueId id);
    boolean existsByIdAttributeKey(String attributeKey);
}
