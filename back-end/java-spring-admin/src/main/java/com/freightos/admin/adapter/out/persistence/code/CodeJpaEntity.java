package com.freightos.admin.adapter.out.persistence.code;

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
@Table(schema = "admin", name = "code")
@Getter
@Setter
@NoArgsConstructor
public class CodeJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_id")
    private Long id;

    @Column(name = "code_group", nullable = false, length = 40)
    private String codeGroup;

    @Column(name = "code_value", nullable = false, length = 40)
    private String codeValue;

    @Column(name = "code_label", nullable = false, length = 200)
    private String codeLabel;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "remark", length = 500)
    private String remark;
}
