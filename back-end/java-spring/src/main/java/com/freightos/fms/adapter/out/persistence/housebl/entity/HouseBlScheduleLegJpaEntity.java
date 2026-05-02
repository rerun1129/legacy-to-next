package com.freightos.fms.adapter.out.persistence.housebl.entity;

import com.freightos.common.persistence.BaseJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * JPA ORM 엔티티 — House B/L 구간별 운항 스케줄 (E-19).
 */
@Entity
@Table(schema = "fms", name = "house_bl_schedule_leg")
@Getter
@NoArgsConstructor
public class HouseBlScheduleLegJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "house_bl_schedule_leg_id", updatable = false, nullable = false)
    private Long houseBlScheduleLegId;

    @Column(name = "house_bl_id", nullable = false, insertable = false, updatable = false)
    private Long houseBlId;

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

    public void setHouseBlScheduleLegId(Long v) { this.houseBlScheduleLegId = v; }
    public void setHouseBlId(Long v) { this.houseBlId = v; }
    public void setToCode(String v) { this.toCode = v; }
    public void setByCarrier(String v) { this.byCarrier = v; }
    public void setFlightNo(String v) { this.flightNo = v; }
    public void setOnBoardDt(String v) { this.onBoardDt = v; }
    public void setOnBoardTm(String v) { this.onBoardTm = v; }
    public void setArrivalDt(String v) { this.arrivalDt = v; }
    public void setArrivalTm(String v) { this.arrivalTm = v; }
}

