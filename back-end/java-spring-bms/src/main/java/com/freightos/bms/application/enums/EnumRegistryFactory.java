package com.freightos.bms.application.enums;

import com.freightos.bms.application.enums.projection.EnumOption;
import com.freightos.bms.domain.common.enums.SortDirection;
import com.freightos.bms.domain.financialdocument.enums.DocumentStatus;
import com.freightos.bms.domain.financialdocument.enums.DocumentType;
import com.freightos.bms.domain.financialdocument.enums.GroupCategory;
import com.freightos.bms.domain.financialdocument.enums.IssueType;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * BMS enum 5종을 Java enum 최종 폴백 레지스트리에 등록한다.
 * Redis → admin DB 체인 이후에도 코드를 찾지 못할 때 사용된다.
 */
@Component
public class EnumRegistryFactory {

    @Bean
    public EnumRegistry enumRegistry() {
        Map<String, List<EnumOption>> map = new LinkedHashMap<>();

        register(map, "DocumentType", DocumentType.values(),
                e -> new EnumOption(e.name(), e.name(), null));
        register(map, "DocumentStatus", DocumentStatus.values(),
                e -> new EnumOption(e.name(), e.name(), null));
        register(map, "GroupCategory", GroupCategory.values(),
                e -> new EnumOption(e.name(), e.name(), null));
        register(map, "IssueType", IssueType.values(),
                e -> new EnumOption(e.name(), e.name(), null));
        register(map, "SortDirection", SortDirection.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));

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
