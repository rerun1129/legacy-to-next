package com.freightos.fms.application.enums;

import com.freightos.common.exception.ResourceNotFoundException;
import com.freightos.fms.domain.enums.EnumOption;
import com.freightos.fms.domain.enums.EnumRegistry;
import com.freightos.fms.domain.enums.port.in.EnumQueryResult;
import com.freightos.fms.domain.enums.port.in.EnumQueryUseCase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EnumQueryService implements EnumQueryUseCase {

    private final EnumRegistry enumRegistry;

    public EnumQueryService(EnumRegistry enumRegistry) {
        this.enumRegistry = enumRegistry;
    }

    @Override
    public List<EnumOption> getByName(String name) {
        return enumRegistry.getByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("EnumRegistry", name));
    }

    @Override
    public EnumQueryResult getByNames(List<String> names) {
        Map<String, List<EnumOption>> found = new LinkedHashMap<>();
        List<String> notFound = new ArrayList<>();

        for (String name : names) {
            enumRegistry.getByName(name).ifPresentOrElse(
                    options -> found.put(name, options),
                    () -> notFound.add(name)
            );
        }

        return new EnumQueryResult(found, notFound);
    }
}
