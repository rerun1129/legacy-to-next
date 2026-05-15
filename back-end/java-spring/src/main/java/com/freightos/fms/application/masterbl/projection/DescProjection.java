package com.freightos.fms.application.masterbl.projection;

/**
 * Master B/L desc(master_bl_desc) application-layer projection.
 * SEA/AIR 공통 사용 — root 레벨에서 노출된다.
 * enum/VO는 String으로 평탄화하여 Adapter(in) 계층 의존을 차단한다.
 */
public record DescProjection(
        String marks,
        String description,
        String descClause1,
        String descClause2
) {
    public static DescProjection empty() {
        return new DescProjection(null, null, null, null);
    }
}
