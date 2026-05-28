package com.freightos.admin.adapter.out.persistence.attributevalue;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "admin", name = "attribute_value",
        uniqueConstraints = @UniqueConstraint(name = "uq_admin_attribute_value_key_value", columnNames = {"attribute_key", "value"}))
@Getter
@Setter
@NoArgsConstructor
public class AttributeValueJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "attribute_key", nullable = false, length = 80)
    private String attributeKey;

    @Column(name = "value", nullable = false, length = 100)
    private String value;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
