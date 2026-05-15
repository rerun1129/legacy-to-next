package com.freightos.fms.adapter.in.web.masterbl.dto;

import com.freightos.fms.application.masterbl.projection.SeaDetailProjection;

import java.math.BigDecimal;

/**
 * Master SEA 본체 상세 응답 DTO. SeaDetailProjection을 1:1 매핑한다.
 * desc는 root MasterBlDetailResponse.DescView로 통합되어 이 DTO에서 제외된다.
 */
public record SeaDetailResponse(
        String loadType,
        String linerCode,
        String vesselCode,
        String vesselName,
        String voyageNo,
        String onboardDate,
        String vesselNationality,
        String serviceTerm,
        String blType,
        String porCode,
        String finalDestCode,
        BigDecimal rton,
        String lineBkgNo,
        String issueDate,
        String remark
) {
    public static SeaDetailResponse from(SeaDetailProjection p) {
        return new SeaDetailResponse(
                p.loadType(),
                p.linerCode(),
                p.vesselCode(),
                p.vesselName(),
                p.voyageNo(),
                p.onboardDate(),
                p.vesselNationality(),
                p.serviceTerm(),
                p.blType(),
                p.porCode(),
                p.finalDestCode(),
                p.rton(),
                p.lineBkgNo(),
                p.issueDate(),
                p.remark()
        );
    }
}
