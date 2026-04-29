package com.freightos.fms.adapter.out.persistence.masterbl.entity;

import com.freightos.fms.adapter.out.persistence.common.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — Master B/L Schedule Leg (E-07).
 * MasterBlJpaEntity 와 @ManyToOne(FK: master_bl_id) 관계.
 * 항공 Master B/L 전용. 각 행이 하나의 leg 레코드.
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

    @Column(name = "to_code", length = 10)
    private String toCode;

    @Column(name = "by_airline", length = 10)
    private String byAirline;

    @Column(name = "flight_no", length = 20)
    private String flightNo;

    @Column(name = "on_board_date", length = 8)
    private String onBoardDate;

    @Column(name = "on_board_time", length = 4)
    private String onBoardTime;

    @Column(name = "arrival_date", length = 8)
    private String arrivalDate;

    @Column(name = "arrival_time", length = 4)
    private String arrivalTime;

    @Column(name = "seq")
    private Integer seq;

    public void setMasterBl(MasterBlJpaEntity v) { this.masterBl = v; }
    public void setToCode(String v) { this.toCode = v; }
    public void setByAirline(String v) { this.byAirline = v; }
    public void setFlightNo(String v) { this.flightNo = v; }
    public void setOnBoardDate(String v) { this.onBoardDate = v; }
    public void setOnBoardTime(String v) { this.onBoardTime = v; }
    public void setArrivalDate(String v) { this.arrivalDate = v; }
    public void setArrivalTime(String v) { this.arrivalTime = v; }
    public void setSeq(Integer v) { this.seq = v; }
}
