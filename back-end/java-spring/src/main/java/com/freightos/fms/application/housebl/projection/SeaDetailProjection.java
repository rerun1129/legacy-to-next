package com.freightos.fms.application.housebl.projection;

import java.math.BigDecimal;
import java.util.List;

/**
 * SEA 본체(HouseBlSea) 상세 필드의 application-layer projection.
 * enum/VO는 String/primitive로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record SeaDetailProjection(
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
        String noOfContainerOrPackages,
        List<SeaContainerProjection> containers,
        SeaDescProjection desc
) {}
