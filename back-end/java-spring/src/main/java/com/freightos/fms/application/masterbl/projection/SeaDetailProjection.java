package com.freightos.fms.application.masterbl.projection;

import java.math.BigDecimal;

/**
 * SEA 본체(MasterBlSea) 상세 필드의 application-layer projection.
 * enum/VO는 String/primitive로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 * Master Sea는 container 미사용, incoterms/deliveryCode 미보유 (Master 도메인 사양).
 */
public record SeaDetailProjection(
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
        SeaDescProjection desc,
        String remark
) {}
