package com.freightos.fms.adapter.in.web.housebl.dto;

import com.freightos.fms.application.housebl.projection.SeaDetailProjection;

import java.math.BigDecimal;

/** SEA 본체 상세 응답 DTO. SeaDetailProjection을 1:1 매핑한다. */
public record SeaDetailResponse(
        String linerCode,
        String vesselCode,
        String vesselName,
        String voyageNo,
        String onboardDate,
        String porCode,
        String finalDestCode,
        String issueDate,
        String noOfBl,
        String issuePlace,
        String doDate,
        String payableAt,
        boolean triangle,
        String serviceTerm,
        String vesselNationality,
        BigDecimal rton,
        String sayInformation,
        String noOfContainerOrPackages
) {
    public static SeaDetailResponse from(SeaDetailProjection p) {
        return new SeaDetailResponse(
                p.linerCode(),
                p.vesselCode(),
                p.vesselName(),
                p.voyageNo(),
                p.onboardDate(),
                p.porCode(),
                p.finalDestCode(),
                p.issueDate(),
                p.noOfBl(),
                p.issuePlace(),
                p.doDate(),
                p.payableAt(),
                p.triangle(),
                p.serviceTerm(),
                p.vesselNationality(),
                p.rton(),
                p.sayInformation(),
                p.noOfContainerOrPackages()
        );
    }
}
