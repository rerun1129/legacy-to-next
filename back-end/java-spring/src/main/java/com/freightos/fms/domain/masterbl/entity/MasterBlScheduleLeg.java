package com.freightos.fms.domain.masterbl.entity;

import com.freightos.fms.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * E-07 Master B/L Schedule Leg (구간 일정).
 * 순수 도메인 엔티티 — JPA 어노테이션 없음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MasterBlScheduleLeg extends BaseEntity {

    private Long masterBlScheduleLegId;
    private Long masterBlId;
    private String toCode;
    private String byCarrier;
    private String flightNo;
    private String onBoardDt;
    private String onBoardTm;
    private String arrivalDt;
    private String arrivalTm;
    private int seq;

    public static MasterBlScheduleLeg create(Long masterBlId, String toCode,
                                             String onBoardDt, String arrivalDt, int seq) {
        MasterBlScheduleLeg leg = new MasterBlScheduleLeg();
        leg.masterBlId = masterBlId;
        leg.toCode     = toCode;
        leg.onBoardDt  = onBoardDt;
        leg.arrivalDt  = arrivalDt;
        leg.seq        = seq;
        return leg;
    }

    public void updateDetails(String toCode, String byCarrier, String flightNo,
                              String onBoardDt, String onBoardTm,
                              String arrivalDt, String arrivalTm, int seq) {
        this.toCode    = toCode;
        this.byCarrier = byCarrier;
        this.flightNo  = flightNo;
        this.onBoardDt = onBoardDt;
        this.onBoardTm = onBoardTm;
        this.arrivalDt = arrivalDt;
        this.arrivalTm = arrivalTm;
        this.seq       = seq;
    }
}
