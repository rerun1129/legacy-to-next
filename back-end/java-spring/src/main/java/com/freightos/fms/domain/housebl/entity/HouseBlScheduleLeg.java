package com.freightos.fms.domain.housebl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-19 House B/L Schedule Leg (항공 전용).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlScheduleLeg extends BaseEntity {

    private Long houseBlId;
    private String toCode;
    private String byCarrier;
    private String flightNo;
    private String onBoardDt;
    private String onBoardTm;
    private String arrivalDt;
    private String arrivalTm;
    private int seq;

    public static HouseBlScheduleLeg create(Long houseBlId, String toCode,
                                            String onBoardDt, String arrivalDt, int seq) {
        HouseBlScheduleLeg leg = new HouseBlScheduleLeg();
        leg.houseBlId  = houseBlId;
        leg.toCode     = toCode;
        leg.onBoardDt  = onBoardDt;
        leg.arrivalDt  = arrivalDt;
        leg.seq        = seq;
        return leg;
    }

    public void updateDetails(String byCarrier, String flightNo, String onBoardTm, String arrivalTm) {
        this.byCarrier = byCarrier;
        this.flightNo  = flightNo;
        this.onBoardTm = onBoardTm;
        this.arrivalTm = arrivalTm;
    }
}
