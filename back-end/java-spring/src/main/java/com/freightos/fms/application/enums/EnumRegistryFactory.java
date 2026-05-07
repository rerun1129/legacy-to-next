package com.freightos.fms.application.enums;

import com.freightos.fms.domain.common.enums.*;
import com.freightos.fms.application.enums.projection.EnumOption;
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

        // common enums — 메타 보유 (code/description 구분)
        register(map, "Per", Per.values(),
                e -> new EnumOption(e.getCode(), e.getDescription(), e.getDescription()));
        // common enums — label 통일
        register(map, "Bound", Bound.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "BlType", BlType.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "FreightTerm", FreightTerm.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "Incoterms", Incoterms.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "ServiceTerm", ServiceTerm.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "RateClass", RateClass.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "WeightUnit", WeightUnit.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "SecurityStatus", SecurityStatus.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "LoadType", LoadType.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "ShipmentType", ShipmentType.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "WorkDivision", WorkDivision.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "SortDirection", SortDirection.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "VolumeDivisor", VolumeDivisor.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        // DescClause1/2의 label은 public 필드
        register(map, "DescClause1", DescClause1.values(),
                e -> new EnumOption(e.name(), e.label, null));
        register(map, "DescClause2", DescClause2.values(),
                e -> new EnumOption(e.name(), e.label, null));
        // housebl enums — 메타 보유 (description)
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
        // housebl enums — label 통일
        register(map, "TruckType", TruckType.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "SalesClass", SalesClass.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "NoOfBl", NoOfBl.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        // 같은 이름의 JobDiv가 housebl/masterbl 양쪽에 있으므로 네임스페이스로 구분
        register(map, "housebl.JobDiv", JobDiv.values(),
                e -> new EnumOption(e.name(), e.getLabel(), null));
        register(map, "masterbl.MasterBlJobDiv", MasterBlJobDiv.values(),
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
