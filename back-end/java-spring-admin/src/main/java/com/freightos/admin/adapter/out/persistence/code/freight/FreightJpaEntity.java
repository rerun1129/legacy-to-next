package com.freightos.admin.adapter.out.persistence.code.freight;

import com.freightos.admin.common.persistence.BaseJpaEntity;
import com.freightos.admin.domain.code.freight.FreightGroup;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(schema = "admin", name = "freight")
@Getter
@Setter
@NoArgsConstructor
public class FreightJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "freight_id")
    private Long id;

    @Column(name = "freight_code", nullable = false, length = 20, updatable = false, unique = true)
    private String freightCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "freight_unit", length = 10)
    private String freightUnit;

    @Enumerated(EnumType.STRING)
    @Column(name = "freight_group", length = 20)
    private FreightGroup freightGroup;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
