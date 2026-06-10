package com.freightos.pms.application.enums;

import com.freightos.pms.application.enums.projection.EnumOption;
import com.freightos.pms.application.pms.AggregationBasis;
import com.freightos.pms.domain.enums.Bound;
import com.freightos.pms.domain.enums.DateKind;
import com.freightos.pms.domain.enums.DocumentStatus;
import com.freightos.pms.domain.enums.DocumentType;
import com.freightos.pms.domain.enums.JobDiv;
import com.freightos.pms.domain.enums.PortKind;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 애플리케이션 기동 시 모든 PMS ENUM을 EnumRegistry에 등록한다.
 * 동일 키가 두 번 등록되면 구성 오류이므로 즉시 예외를 발생시킨다.
 */
@Component
public class EnumRegistryFactory {

    @Bean
    public EnumRegistry enumRegistry() {
        Map<String, List<EnumOption>> map = new LinkedHashMap<>();

        register(map, "AggregationBasis", AggregationBasis.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null, e.getLabelKo()));
        register(map, "JobDiv", JobDiv.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null, e.getLabelKo()));
        register(map, "Bound", Bound.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null, e.getLabelKo()));
        register(map, "DateKind", DateKind.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null, e.getLabelKo()));
        register(map, "PortKind", PortKind.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null, e.getLabelKo()));
        register(map, "DocumentType", DocumentType.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null, e.getLabelKo()));
        register(map, "DocumentStatus", DocumentStatus.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null, e.getLabelKo()));

        return EnumRegistry.of(map);
    }

    private <T extends Enum<T>> void register(
            Map<String, List<EnumOption>> map,
            String key,
            T[] values,
            Function<T, EnumOption> mapper) {

        if (values.length == 0) {
            return;
        }
        if (map.containsKey(key)) {
            throw new IllegalStateException("Duplicate enum key: " + key);
        }
        List<EnumOption> options = new ArrayList<>(values.length);
        for (T value : values) {
            options.add(mapper.apply(value));
        }
        map.put(key, options);
    }
}
