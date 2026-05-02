package com.freightos.fms.domain.housebl.entity;

import com.freightos.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-19 House B/L 구간별 운항 스케줄.
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HouseBlScheduleLeg extends BaseEntity {

    private Long houseBlScheduleLegId;
    private Long houseBlId;
    private String toCode;
    private String byCarrier;
    private String flightNo;
    private String onBoardDt;
    private String onBoardTm;
    private String arrivalDt;
    private String arrivalTm;

    public static HouseBlScheduleLeg create(Long houseBlId, String toCode,
                                            String onBoardDt, String arrivalDt) {
        HouseBlScheduleLeg s = new HouseBlScheduleLeg();
        s.houseBlId  = houseBlId;
        s.toCode     = toCode;
        s.onBoardDt  = onBoardDt;
        s.arrivalDt  = arrivalDt;
        return s;
    }

    public void updateDetails(String toCode, String byCarrier, String flightNo,
                              String onBoardDt, String onBoardTm,
                              String arrivalDt, String arrivalTm) {
        this.toCode    = toCode;
        this.byCarrier = byCarrier;
        this.flightNo  = flightNo;
        this.onBoardDt = onBoardDt;
        this.onBoardTm = onBoardTm;
        this.arrivalDt = arrivalDt;
        this.arrivalTm = arrivalTm;
    }
}
