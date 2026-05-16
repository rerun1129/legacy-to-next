package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

/**
 * JPA ORM 엔티티 — Master B/L Schedule Leg (구간 일정).
 * MasterBlAirJpaEntity 와 @OneToMany(FK: master_bl_air_id) 관계.
 * FK가 master_bl_id → master_bl_air_id 로 재배치됨.
 */
@Entity
@Table(schema = "fms", name = "master_bl_schedule_leg")
@Getter
@NoArgsConstructor
@DynamicUpdate
public class MasterBlScheduleLegJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_schedule_leg_id", updatable = false, nullable = false)
    private Long masterBlScheduleLegId;

    @Column(name = "master_bl_air_id", nullable = false, insertable = false, updatable = false)
    private Long masterBlAirId;

    @Column(name = "to_code", nullable = false, length = 10)
    private String toCode;

    @Column(name = "by_carrier", length = 20)
    private String byCarrier;

    @Column(name = "flight_no", length = 20)
    private String flightNo;

    @Column(name = "on_board_dt", nullable = false, length = 8)
    private String onBoardDt;

    @Column(name = "on_board_tm", length = 4)
    private String onBoardTm;

    @Column(name = "arrival_dt", nullable = false, length = 8)
    private String arrivalDt;

    @Column(name = "arrival_tm", length = 4)
    private String arrivalTm;

    public void setMasterBlScheduleLegId(Long v) { this.masterBlScheduleLegId = v; }
    public void setToCode(String v) { this.toCode = v; }
    public void setByCarrier(String v) { this.byCarrier = v; }
    public void setFlightNo(String v) { this.flightNo = v; }
    public void setOnBoardDt(String v) { this.onBoardDt = v; }
    public void setOnBoardTm(String v) { this.onBoardTm = v; }
    public void setArrivalDt(String v) { this.arrivalDt = v; }
    public void setArrivalTm(String v) { this.arrivalTm = v; }
}
