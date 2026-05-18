package com.freightos.admin.adapter.out.persistence.attributevalue;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "admin", name = "attribute_value")
@Getter
@Setter
@NoArgsConstructor
public class AttributeValueJpaEntity extends BaseJpaEntity {

    @EmbeddedId
    private AttributeValueId id;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
