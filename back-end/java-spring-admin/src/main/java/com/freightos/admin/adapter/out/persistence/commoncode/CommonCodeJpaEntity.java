package com.freightos.admin.adapter.out.persistence.commoncode;

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
@Table(schema = "admin", name = "common_code",
        uniqueConstraints = @UniqueConstraint(name = "uq_admin_common_code_group_code_code",
                columnNames = {"group_code", "code"}))
@Getter
@Setter
@NoArgsConstructor
public class CommonCodeJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "common_code_id")
    private Long commonCodeId;

    @Column(name = "group_code", nullable = false, length = 80)
    private String groupCode;

    @Column(name = "code", nullable = false, length = 80)
    private String code;

    @Column(name = "label", nullable = false, length = 200)
    private String label;

    @Column(name = "label_ko", length = 200)
    private String labelKo;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
