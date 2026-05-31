package com.freightos.fms.application.masterbl.projection;

/**
 * Master B/L 단건 조회 응답 전용 뷰 타입.
 * MasterBlDetailResult(코드만) + code→name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 MasterBlDetailResponse DTO로 변환한다.
 */
public record MasterBlDetailView(
        MasterBlDetailResult base,

        // hs_code → name (findHsCodeNames)
        String hsCodeName,

        // team_code → name (findTeamNames)
        String teamName
) {}
