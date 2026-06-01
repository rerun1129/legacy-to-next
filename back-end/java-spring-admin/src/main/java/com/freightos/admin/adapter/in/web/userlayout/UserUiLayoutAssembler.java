package com.freightos.admin.adapter.in.web.userlayout;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.freightos.admin.adapter.in.web.userlayout.dto.SaveLayoutRequest;
import com.freightos.admin.common.exception.ApplicationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUiLayoutAssembler {

    private final ObjectMapper objectMapper;

    /** SaveLayoutRequest.payload(JsonNode) → use-case 경계의 String 변환. */
    public String toPayloadString(SaveLayoutRequest request) {
        try {
            return objectMapper.writeValueAsString(request.payload());
        } catch (JsonProcessingException e) {
            throw ApplicationException.badRequest("UI_LAYOUT_PAYLOAD_INVALID", "payload JSON 직렬화에 실패했습니다.");
        }
    }

    /**
     * use-case가 반환한 payload JSON 문자열 → JsonNode 변환.
     * 컨트롤러가 따옴표로 감싼 문자열이 아닌 JSON 객체로 반환할 수 있도록 파싱한다.
     */
    public JsonNode toJsonNode(String payloadJson) {
        try {
            return objectMapper.readTree(payloadJson);
        } catch (JsonProcessingException e) {
            throw ApplicationException.badRequest("UI_LAYOUT_PAYLOAD_PARSE_ERROR", "저장된 payload 파싱에 실패했습니다.");
        }
    }
}
