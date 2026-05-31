package com.freightos.fms.application.housebl.projection;

/**
 * House B/L 단건 조회 응답 전용 뷰 타입.
 * HouseBlDetailResult(코드만) + code→name name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 HouseBlDetailResponse DTO로 변환한다.
 */
public record HouseBlDetailView(
        HouseBlDetailResult base,

        // customer name 6종 (findCustomerNames)
        String shipperName,
        String consigneeName,
        String notifyName,
        String docPartnerName,
        String settlePartnerName,
        String actualCustomerName,

        // port name 2종 top-level (findPortNames)
        String polName,
        String podName,

        // admin_user name 2종 (findUserNames)
        String salesManName,
        String operatorName,

        // team_code → name (findTeamNames)
        String teamName,

        // SEA detail port name 3종 (findPortNames — pol/pod와 합쳐 1회 조회)
        String issuePlaceName,
        String payableAtName,
        String deliveryName,

        // SEA detail liner name (findCarrierNames)
        String linerName,

        // hs_code → name (findHsCodeNames)
        String hsCodeName
) {}
