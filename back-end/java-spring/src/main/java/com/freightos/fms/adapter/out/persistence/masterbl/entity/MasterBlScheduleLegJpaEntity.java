package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Master B/L Schedule Leg (구간 일정).
 * MasterBlJpaEntity 와 @ManyToOne(FK: master_bl_id) 관계.
 */
@Entity
@Table(name = "master_bl_schedule_leg")
@Getter
@NoArgsConstructor
public class MasterBlScheduleLegJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "master_bl_schedule_leg_id", updatable = false, nullable = false)
    private Long masterBlScheduleLegId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "master_bl_id", nullable = false)
    private MasterBlJpaEntity masterBl;

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

    @Column(name = "seq", nullable = false)
    private int seq;

    public void setMasterBl(MasterBlJpaEntity v) { this.masterBl = v; }
    public void setToCode(String v) { this.toCode = v; }
    public void setByCarrier(String v) { this.byCarrier = v; }
    public void setFlightNo(String v) { this.flightNo = v; }
    public void setOnBoardDt(String v) { this.onBoardDt = v; }
    public void setOnBoardTm(String v) { this.onBoardTm = v; }
    public void setArrivalDt(String v) { this.arrivalDt = v; }
    public void setArrivalTm(String v) { this.arrivalTm = v; }
    public void setSeq(int v) { this.seq = v; }
}
