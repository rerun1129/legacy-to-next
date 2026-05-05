package com.freightos.fms.application.enums;

import com.freightos.fms.domain.common.enums.*;
import com.freightos.fms.domain.enums.EnumOption;
import com.freightos.fms.domain.enums.EnumRegistry;
import com.freightos.fms.domain.housebl.enums.*;
import com.freightos.fms.domain.masterbl.enums.MasterBlJobDiv;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 애플리케이션 기동 시 모든 ENUM을 EnumRegistry에 등록한다.
 * 동일 키가 두 번 등록되면 구성 오류이므로 즉시 예외를 발생시킨다.
 */
@Component
public class EnumRegistryFactory {

    @Bean
    public EnumRegistry enumRegistry() {
        Map<String, List<EnumOption>> map = new LinkedHashMap<>();

        // common enums
        register(map, "Per", Per.values(),
                e -> new EnumOption(e.getCode(), e.getDescription(), e.getDescription()));
        register(map, "Bound", Bound.values(), EnumOption::fromName);
        register(map, "BlType", BlType.values(), EnumOption::fromName);
        register(map, "FreightTerm", FreightTerm.values(), EnumOption::fromName);
        register(map, "Incoterms", Incoterms.values(), EnumOption::fromName);
        register(map, "ServiceTerm", ServiceTerm.values(), EnumOption::fromName);
        register(map, "RateClass", RateClass.values(), EnumOption::fromName);
        register(map, "WeightUnit", WeightUnit.values(), EnumOption::fromName);
        register(map, "SecurityStatus", SecurityStatus.values(), EnumOption::fromName);
        register(map, "LoadType", LoadType.values(), EnumOption::fromName);
        register(map, "ShipmentType", ShipmentType.values(), EnumOption::fromName);
        register(map, "SortDirection", SortDirection.values(), EnumOption::fromName);
        register(map, "VolumeDivisor", VolumeDivisor.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        // DescClause1/2의 label은 public 필드
        register(map, "DescClause1", DescClause1.values(),
                e -> new EnumOption(e.name(), e.label, null));
        register(map, "DescClause2", DescClause2.values(),
                e -> new EnumOption(e.name(), e.label, null));
        // housebl enums with description
        register(map, "Fhd", Fhd.values(),
                e -> new EnumOption(e.name(), e.getDescription(), e.getDescription()));
        register(map, "FlightType", FlightType.values(),
                e -> new EnumOption(e.name(), e.getDescription(), e.getDescription()));
        register(map, "FreightCondition", FreightCondition.values(),
                e -> new EnumOption(e.name(), e.getDescription(), e.getDescription()));
        register(map, "CargoType", CargoType.values(),
                e -> new EnumOption(e.getCode(), e.getDescription(), e.getDescription()));
        register(map, "HandlingInfoCode", HandlingInfoCode.values(),
                e -> new EnumOption(e.getCode(), e.getDescription(), e.getDescription()));
        register(map, "ContainerType", ContainerType.values(),
                e -> new EnumOption(e.getCode(), e.getDescription(), e.getDescription()));
        // TruckType: code만 있고 별도 description 없으므로 code를 label로 사용
        register(map, "TruckType", TruckType.values(),
                e -> new EnumOption(e.getCode(), e.getCode(), null));
        register(map, "SalesClass", SalesClass.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "NoOfBl", NoOfBl.values(),
                e -> new EnumOption(String.valueOf(e.getNumber()), String.valueOf(e.getNumber()), null));
        // 같은 이름의 JobDiv가 housebl/masterbl 양쪽에 있으므로 네임스페이스로 구분
        register(map, "housebl.JobDiv", JobDiv.values(), EnumOption::fromName);
        register(map, "masterbl.MasterBlJobDiv", MasterBlJobDiv.values(), EnumOption::fromName);
        // PackageUnit은 values().length == 0이므로 register 내부에서 skip됨
        register(map, "PackageUnit", PackageUnit.values(), EnumOption::fromName);

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
            throw new IllegalStateException("중복 ENUM 키: " + key);
        }
        List<EnumOption> options = new ArrayList<>(values.length);
        for (T value : values) {
            options.add(mapper.apply(value));
        }
        map.put(key, options);
    }
}
