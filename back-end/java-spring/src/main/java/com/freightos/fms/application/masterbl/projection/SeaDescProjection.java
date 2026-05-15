package com.freightos.fms.application.masterbl.projection;

/**
 * SEA desc(master_bl_sea_desc) application-layer projection.
 * enum/VO는 String으로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record SeaDescProjection(
        String marks,
        String description,
        String descClause1,
        String descClause2
) {
    public static SeaDescProjection empty() {
        return new SeaDescProjection(null, null, null, null);
    }
}
