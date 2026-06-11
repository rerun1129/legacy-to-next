package com.freightos.pms.application.enums;

import com.freightos.pms.application.enums.port.out.CommonCodeCachePort;
import com.freightos.pms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.pms.application.enums.projection.EnumOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 공통코드 폴백 체인: Redis → admin.common_code DB → Java enum 레지스트리.
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

    public Optional<List<EnumOption>> resolve(String groupCode) {
        Optional<List<EnumOption>> cached = cachePort.get(groupCode);
        if (cached.isPresent()) {
            return cached;
        }

        Optional<List<EnumOption>> fromDb = dbPort.findByGroupCode(groupCode);
        if (fromDb.isPresent()) {
            cachePort.put(groupCode, fromDb.get());
            return fromDb;
        }

        log.debug("CommonCode group '{}' not found in Redis nor DB — falling back to Java enum", groupCode);
        return Optional.empty();
    }
}
