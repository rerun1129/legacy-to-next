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
 * fms.house_bl_truck 읽기 전용 참조 엔티티.
 * TRUCK 모드의 charge_weight_kg·load_type·vessel_name·voyage_no 조회에 사용.
 * house_bl_id = house_bl의 FK이자 PK(1:1 확장 테이블).
 */
@Entity
@Immutable
@Table(schema = "fms", name = "house_bl_truck")
@Getter
@NoArgsConstructor
public class PmsHouseBlTruckRefJpaEntity {

    @Id
    @Column(name = "house_bl_id")
    private Long houseBlId;

    @Column(name = "charge_weight_kg", precision = 12, scale = 4)
    private BigDecimal chargeWeightKg;

    @Column(name = "load_type", length = 10)
    private String loadType;

    @Column(name = "vessel_name", length = 100)
    private String vesselName;

    @Column(name = "voyage_no", length = 50)
    private String voyageNo;
}
