package com.freightos.fms.application.enums.port.out;

import com.freightos.fms.application.enums.projection.EnumOption;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 Redis 캐시 아웃바운드 포트.
 * 캐시 키: cc:{groupCode}
 */
public interface CommonCodeCachePort {

    /**
     * Redis에서 공통코드 목록을 조회한다.
     * 미스이거나 타임아웃/예외면 빈 Optional을 반환한다(예외 전파 없음).
     */
    Optional<List<EnumOption>> get(String groupCode);

    /**
     * Redis에 공통코드 목록을 저장한다(TTL 24h).
     * 실패 시 로그 경고 후 무시한다(예외 전파 없음).
     */
    void put(String groupCode, List<EnumOption> options);
}
