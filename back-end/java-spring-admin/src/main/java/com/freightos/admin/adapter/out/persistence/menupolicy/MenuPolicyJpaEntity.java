package com.freightos.admin.adapter.out.persistence.menupolicy;

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
@Table(schema = "admin", name = "menu_policy",
        uniqueConstraints = @UniqueConstraint(columnNames = {"menu_id", "attribute_key", "required_value"}))
@Getter
@Setter
@NoArgsConstructor
public class MenuPolicyJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "policy_id")
    private Long id;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "attribute_key", nullable = false, length = 80)
    private String attributeKey;

    @Column(name = "required_value", nullable = false, length = 100)
    private String requiredValue;
}
