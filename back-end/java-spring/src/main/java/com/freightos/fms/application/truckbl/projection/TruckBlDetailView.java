package com.freightos.fms.application.truckbl.projection;

import com.freightos.fms.application.freight.FreightView;

/**
 * Truck B/L 단건 조회 응답 전용 뷰 타입.
 * TruckBlDetailResult(코드만) + code→name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 TruckBlDetailResponse DTO로 변환한다.
 */
public record TruckBlDetailView(
        TruckBlDetailResult base,

        // hs_code → name (findHsCodeNames)
        String hsCodeName,

        // team_code → name (findTeamNames)
        String teamName,

        // Freight 탭 헤더+라인 (없으면 null)
        FreightView freight
) {}
