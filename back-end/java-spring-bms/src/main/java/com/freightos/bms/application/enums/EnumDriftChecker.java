package com.freightos.bms.application.enums;

import com.freightos.bms.application.enums.port.out.CommonCodeReadPort;
import com.freightos.bms.application.enums.projection.EnumOption;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 기동 직후 Java enum 상수 집합과 admin DB 공통코드 집합을 비교한다.
 */
@Slf4j
@Component
public class EnumDriftChecker {

    private final EnumRegistry enumRegistry;
    private final CommonCodeReadPort dbPort;

    public EnumDriftChecker(EnumRegistry enumRegistry, CommonCodeReadPort dbPort) {
        this.enumRegistry = enumRegistry;
        this.dbPort        = dbPort;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkDrift() {
        for (String groupCode : enumRegistry.getAllKeys()) {
            checkGroup(groupCode);
        }
    }

    private void checkGroup(String groupCode) {
        Optional<List<EnumOption>> dbOptions;
        try {
            dbOptions = dbPort.findByGroupCode(groupCode);
        } catch (Exception e) {
            log.warn("[EnumDrift] DB unreachable for group '{}' — skipping drift check: {}", groupCode, e.getMessage());
            return;
        }

        if (dbOptions.isEmpty()) {
            return;
        }

        Set<String> javaKeys = enumRegistry.getByName(groupCode)
                .map(opts -> opts.stream().map(EnumOption::code).collect(Collectors.toSet()))
                .orElse(Set.of());

        Set<String> dbKeys = dbOptions.get().stream()
                .map(EnumOption::code)
                .collect(Collectors.toSet());

        Set<String> onlyInJava = javaKeys.stream().filter(k -> !dbKeys.contains(k)).collect(Collectors.toSet());
        Set<String> onlyInDb   = dbKeys.stream().filter(k -> !javaKeys.contains(k)).collect(Collectors.toSet());

        if (!onlyInJava.isEmpty()) {
            log.warn("[EnumDrift] group='{}' Java에만 있는 코드(DB 누락): {}", groupCode, onlyInJava);
        }
        if (!onlyInDb.isEmpty()) {
            log.warn("[EnumDrift] group='{}' DB에만 있는 코드(Java 미선언): {}", groupCode, onlyInDb);
        }
    }
}
