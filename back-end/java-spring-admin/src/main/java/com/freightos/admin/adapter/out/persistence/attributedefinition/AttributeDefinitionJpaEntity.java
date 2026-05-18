package com.freightos.admin.adapter.out.persistence.attributedefinition;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "admin", name = "attribute_definition")
@Getter
@Setter
@NoArgsConstructor
public class AttributeDefinitionJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_id")
    private Long id;

    @Column(name = "attribute_key", nullable = false, length = 80, updatable = false)
    private String attributeKey;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "value_type", nullable = false, length = 20)
    private String valueType;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
