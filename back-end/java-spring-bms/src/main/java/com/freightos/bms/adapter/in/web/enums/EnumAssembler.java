package com.freightos.bms.adapter.in.web.enums;

import com.freightos.bms.adapter.in.web.enums.dto.EnumMapResponse;
import com.freightos.bms.adapter.in.web.enums.dto.EnumOptionResponse;
import com.freightos.bms.application.enums.port.in.EnumQueryResult;
import com.freightos.bms.application.enums.projection.EnumOption;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

    public String computeEtag(Object payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString().substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }
}
