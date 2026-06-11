package com.freightos.pms.application.enums;

import java.util.Map;

/**
 * PMS API 키 → admin DB group_code 별칭 매핑 SSOT.
 *
 * 배경: FMS·PMS는 admin.common_code의 그룹을 공유한다.
 * 일부 그룹 코드는 DB에 네임스페이스 접두사(예: "housebl.")가 포함되어 있어
 * PMS가 짧은 API 키("JobDiv")로 조회하면 Redis/DB에서 미스가 발생한다.
 * 이 맵을 통해 API 키를 실제 DB group_code로 변환하여
 * FMS write-through 캐시(cc:housebl.JobDiv)와 동일한 키를 사용한다.
 */
final class CommonCodeGroupAliases {

    // API 키(PMS 레지스트리 키) → DB group_code(admin.common_code.group_code)
    private static final Map<String, String> ALIASES = Map.of(
            "JobDiv", "housebl.JobDiv"
    );

    private CommonCodeGroupAliases() {}

    /**
     * API 키를 DB group_code로 변환한다.
     * 등록되지 않은 키는 그대로 반환하므로 기존 6개 그룹(AggregationBasis/Bound/DateKind/PortKind/DocumentType/DocumentStatus)은 영향 없다.
     */
    static String toDbGroup(String apiKey) {
        return ALIASES.getOrDefault(apiKey, apiKey);
    }

    /**
     * 별칭 등록 여부를 확인한다.
     */
    static boolean hasAlias(String apiKey) {
        return ALIASES.containsKey(apiKey);
    }
}
