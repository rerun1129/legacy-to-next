package com.freightos.fms.application.masterbl.projection;

import com.freightos.fms.application.freight.FreightView;

/**
 * Master B/L 단건 조회 응답 전용 뷰 타입.
 * MasterBlDetailResult(코드만) + code→name 필드 + freight 조회 결과를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 MasterBlDetailResponse DTO로 변환한다.
 */
public record MasterBlDetailView(
        MasterBlDetailResult base,

        // hs_code → name (findHsCodeNames)
        String hsCodeName,

        // team_code → name (findTeamNames)
        String teamName,

        // Freight 탭 조회 결과 (헤더+라인 없으면 null)
        FreightView freight
) {
    /**
     * freight 미지정 편의 생성자 — freight=null로 위임.
     * 기존 테스트의 positional 3-인자 호출을 보존한다.
     */
    public MasterBlDetailView(MasterBlDetailResult base, String hsCodeName, String teamName) {
        this(base, hsCodeName, teamName, null);
    }
}
