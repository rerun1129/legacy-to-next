package com.freightos.admin.adapter.out.persistence.menu;

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
@Table(schema = "admin", name = "menu")
@Getter
@Setter
@NoArgsConstructor
public class MenuJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menu_id")
    private Long id;

    @Column(name = "menu_code", nullable = false, length = 80, updatable = false)
    private String menuCode;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "path", length = 200)
    private String path;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @Column(name = "label_en", length = 200)
    private String labelEn;

    @Column(name = "icon", length = 100)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "module_code", nullable = false, length = 40)
    private String moduleCode;
}
