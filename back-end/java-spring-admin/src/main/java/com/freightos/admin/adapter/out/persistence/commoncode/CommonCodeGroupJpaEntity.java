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
@Table(schema = "admin", name = "common_code_group",
        uniqueConstraints = @UniqueConstraint(name = "uq_admin_common_code_group_code", columnNames = {"group_code"}))
@Getter
@Setter
@NoArgsConstructor
public class CommonCodeGroupJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "common_code_group_id")
    private Long commonCodeGroupId;

    @Column(name = "group_code", nullable = false, length = 80)
    private String groupCode;

    @Column(name = "source_module", nullable = false, length = 10)
    private String sourceModule;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active;
}
