package com.freightos.fms.application.masterbl.projection;

import java.math.BigDecimal;

/**
 * AIR 본체(MasterBlAir) 상세 필드의 application-layer projection.
 * enum/VO는 String/primitive로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 * desc는 SEA/AIR 공통 필드로 root MasterBlDetailResult.desc에서 노출된다 (§6.49 ㉕).
 * AIR은 container 미사용 — container 필드 없음 (§13.9).
 */
public record AirDetailProjection(
        String airlineCode,
        BigDecimal chargeWeightKg,
        BigDecimal volumeWeightKg,
        String rateClass,
        String currencyCode,
        String declaredValueCarriage,
        String declaredValueCustoms,
        String insurance,
        String accountInformation,
        String securityStatus,
        String flightType,
        String issueDate,
        String issuePlace,
        String signature,
        String otherTerm,
        String handlingInfoCode,
        String handlingInfoText,
        String remark
) {
    /** §6.55 — nested object null 방지용 empty 팩토리. */
    public static AirDetailProjection empty() {
        return new AirDetailProjection(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
}
