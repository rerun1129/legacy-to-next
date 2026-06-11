package com.freightos.pms.application.enums.port.out;

import com.freightos.pms.application.enums.projection.EnumOption;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 Redis 캐시 아웃바운드 포트.
 */
public interface CommonCodeCachePort {

    Optional<List<EnumOption>> get(String groupCode);

    void put(String groupCode, List<EnumOption> options);
}
