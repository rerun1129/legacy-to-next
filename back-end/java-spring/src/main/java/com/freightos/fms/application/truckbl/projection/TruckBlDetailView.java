package com.freightos.fms.application.truckbl.projection;

/**
 * Truck B/L 단건 조회 응답 전용 뷰 타입.
 * TruckBlDetailResult(코드만) + code→name name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 TruckBlDetailResponse DTO로 변환한다.
 */
public record TruckBlDetailView(
        TruckBlDetailResult base,

        // hs_code → name (findHsCodeNames)
        String hsCodeName
) {}
