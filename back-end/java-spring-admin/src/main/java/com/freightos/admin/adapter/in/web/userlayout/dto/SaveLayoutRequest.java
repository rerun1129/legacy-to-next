package com.freightos.admin.adapter.in.web.userlayout.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;

/**
 * PUT /{storageKey} 요청 바디.
 * payload는 불투명 JSON 객체 — 구조를 검증하지 않는다.
 */
public record SaveLayoutRequest(
        @NotNull JsonNode payload
) {
}
