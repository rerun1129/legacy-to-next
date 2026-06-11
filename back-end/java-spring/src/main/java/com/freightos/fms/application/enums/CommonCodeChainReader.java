package com.freightos.fms.application.enums;

import com.freightos.fms.application.enums.port.out.CommonCodeCachePort;
import com.freightos.fms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.fms.application.enums.projection.EnumOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 폴백 체인: Redis → admin.common_code DB → Java enum 레지스트리.
 *
 * 1단계: Redis 캐시 조회
 * 2단계: Redis 미스 → admin DB 직조회 + Redis에 read-through SET
 * 3단계: DB도 빈값 → 빈 Optional 반환(호출처에서 Java enum 레지스트리 사용)
 */
@Slf4j
@Component
public class CommonCodeChainReader {

    private final CommonCodeCachePort cachePort;
    private final CommonCodeReadPort  dbPort;

    public CommonCodeChainReader(CommonCodeCachePort cachePort, CommonCodeReadPort dbPort) {
        this.cachePort = cachePort;
        this.dbPort    = dbPort;
    }

    /**
     * 폴백 체인으로 공통코드를 조회한다.
     * 결과가 없으면 빈 Optional을 반환하고, 호출처에서 Java enum 폴백을 처리한다.
     */
    public Optional<List<EnumOption>> resolve(String groupCode) {
        Optional<List<EnumOption>> cached = cachePort.get(groupCode);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<List<EnumOption>> fromDb = dbPort.findByGroupCode(groupCode);
        if (fromDb.isPresent()) {
            // read-through: DB 결과를 Redis에 저장(실패는 삼킴)
            cachePort.put(groupCode, fromDb.get());
            return fromDb;
        }

        log.debug("CommonCode group '{}' not found in Redis nor DB — falling back to Java enum", groupCode);
        return Optional.empty();
    }
}
