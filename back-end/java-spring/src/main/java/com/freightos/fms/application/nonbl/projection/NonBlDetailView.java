package com.freightos.fms.application.nonbl.projection;

/**
 * Non B/L 단건 조회 응답 전용 뷰 타입.
 * NonBlDetailResult(코드만) + code→name 필드를 합친 Service 출력 경계 타입.
 * Assembler가 이 타입을 NonBlDetailResponse DTO로 변환한다.
 */
public record NonBlDetailView(
        NonBlDetailResult base,

        // hs_code → name (findHsCodeNames)
        String hsCodeName
) {}
