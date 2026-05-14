package com.freightos.fms.application.housebl.projection;

/**
 * AIR 구간별 운항 스케줄(house_bl_schedule_leg) application-layer projection.
 * enum/VO는 String/primitive로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record AirScheduleLegProjection(
        Long id,
        String toCode,
        String byCarrier,
        String flightNo,
        String onBoardDt,
        String onBoardTm,
        String arrivalDt,
        String arrivalTm
) {}
