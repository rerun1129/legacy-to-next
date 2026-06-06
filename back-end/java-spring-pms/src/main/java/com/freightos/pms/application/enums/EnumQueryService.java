package com.freightos.pms.application.enums;

import com.freightos.common.exception.FmsException;
import com.freightos.pms.application.enums.port.in.EnumQueryResult;
import com.freightos.pms.application.enums.port.in.EnumQueryUseCase;
import com.freightos.pms.application.enums.projection.EnumOption;
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
                .orElseThrow(() -> FmsException.notFound("EnumRegistry not found: " + name));
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
