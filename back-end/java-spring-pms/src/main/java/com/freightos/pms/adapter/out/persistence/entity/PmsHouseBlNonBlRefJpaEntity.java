package com.freightos.pms.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;

/**
 * fms.house_bl_non_bl 읽기 전용 참조 엔티티.
 * NON_BL 모드의 rton 조회에 사용.
 * house_bl_id = house_bl의 FK이자 PK(1:1 확장 테이블).
 */
@Entity
@Immutable
@Table(schema = "fms", name = "house_bl_non_bl")
@Getter
@NoArgsConstructor
public class PmsHouseBlNonBlRefJpaEntity {

    @Id
    @Column(name = "house_bl_id")
    private Long houseBlId;

    @Column(name = "rton", precision = 12, scale = 4)
    private BigDecimal rton;
}
