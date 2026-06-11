package com.freightos.bms.application.enums;

import com.freightos.bms.application.enums.port.in.EnumQueryResult;
import com.freightos.bms.application.enums.port.in.EnumQueryUseCase;
import com.freightos.bms.application.enums.projection.EnumOption;
import com.freightos.common.exception.FmsException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 공통코드 폴백 체인으로 ENUM 옵션을 서빙한다.
 */
@Service
public class EnumQueryService implements EnumQueryUseCase {

    private final CommonCodeChainReader chainReader;
    private final EnumRegistry enumRegistry;

    public EnumQueryService(CommonCodeChainReader chainReader, EnumRegistry enumRegistry) {
        this.chainReader  = chainReader;
        this.enumRegistry = enumRegistry;
    }

    @Override
    public List<EnumOption> getByName(String name) {
        return resolveByName(name)
                .orElseThrow(() -> new FmsException(HttpStatus.NOT_FOUND, "EnumRegistry not found: " + name));
    }

    @Override
    public EnumQueryResult getByNames(List<String> names) {
        Map<String, List<EnumOption>> found = new LinkedHashMap<>();
        List<String> notFound = new ArrayList<>();

        for (String name : names) {
            resolveByName(name).ifPresentOrElse(
                    options -> found.put(name, options),
                    () -> notFound.add(name));
        }

        return new EnumQueryResult(found, notFound);
    }

    private Optional<List<EnumOption>> resolveByName(String name) {
        Optional<List<EnumOption>> fromChain = chainReader.resolve(name);
        if (fromChain.isPresent()) {
            return fromChain;
        }
        return enumRegistry.getByName(name);
    }
}
