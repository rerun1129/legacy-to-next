package com.freightos.fms.application.masterbl.projection;

/**
 * Master B/L Schedule Leg(구간 일정) 항목의 application-layer projection.
 * 도메인 엔티티 MasterBlScheduleLeg를 Adapter(in) 경계에서 사용할 수 있도록 평탄화한다.
 */
public record ScheduleLegProjection(
        Long id,
        String toCode,
        String byCarrier,
        String flightNo,
        String onBoardDt,
        String onBoardTm,
        String arrivalDt,
        String arrivalTm
) {}
