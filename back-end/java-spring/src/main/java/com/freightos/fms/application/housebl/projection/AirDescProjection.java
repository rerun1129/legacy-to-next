package com.freightos.fms.application.housebl.projection;

/**
 * AIR desc(house_bl_desc) application-layer projection.
 * enum/VO는 String으로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record AirDescProjection(
        String marks,
        String description,
        String descClause1,
        String descClause2
) {
    /** desc row가 없을 때 Adapter(in) 계층에 null 대신 반환하는 빈 projection (§6.55 SSOT). */
    public static AirDescProjection empty() {
        return new AirDescProjection(null, null, null, null);
    }
}
