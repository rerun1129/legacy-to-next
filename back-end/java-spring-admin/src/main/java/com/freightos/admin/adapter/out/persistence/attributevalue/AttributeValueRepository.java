package com.freightos.admin.adapter.out.persistence.attributevalue;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValueJpaEntity, Long>, AttributeValueRepositoryCustom {
    Optional<AttributeValueJpaEntity> findByAttributeKeyAndValue(String attributeKey, String value);
    boolean existsByAttributeKeyAndValue(String attributeKey, String value);
    boolean existsByAttributeKeyAndValueAndIdNot(String attributeKey, String value, Long id);
    boolean existsByAttributeKey(String attributeKey);
    List<AttributeValueJpaEntity> findByAttributeKeyAndActiveTrue(String attributeKey);
}
