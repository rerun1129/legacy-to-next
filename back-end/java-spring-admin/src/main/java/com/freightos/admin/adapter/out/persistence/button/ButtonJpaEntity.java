package com.freightos.admin.adapter.out.persistence.button;

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
@Table(schema = "admin", name = "button")
@Getter
@Setter
@NoArgsConstructor
public class ButtonJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "button_id")
    private Long id;

    @Column(name = "button_code", nullable = false, length = 80, updatable = false)
    private String buttonCode;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @Column(name = "action_type", nullable = false, length = 20)
    private String actionType;

    @Column(name = "api_method", length = 10)
    private String apiMethod;

    @Column(name = "api_path", length = 200)
    private String apiPath;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
