package com.freightos.fms.adapter.in.web.enums;

import com.freightos.fms.adapter.in.web.enums.dto.EnumMapResponse;
import com.freightos.fms.adapter.in.web.enums.dto.EnumOptionResponse;
import com.freightos.fms.domain.enums.EnumOption;
import com.freightos.fms.domain.enums.port.in.EnumQueryResult;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EnumAssembler {

    public List<EnumOptionResponse> toResponse(List<EnumOption> options) {
        return options.stream()
                .map(EnumOptionResponse::from)
                .toList();
    }

    public EnumMapResponse toMapResponse(EnumQueryResult result) {
        Map<String, List<EnumOptionResponse>> converted = new LinkedHashMap<>();
        result.found().forEach((key, options) ->
                converted.put(key, toResponse(options)));
        return new EnumMapResponse(converted, result.notFound());
    }
}
