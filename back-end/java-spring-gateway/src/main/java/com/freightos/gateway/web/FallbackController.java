package com.freightos.gateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 서킷브레이커 fallback 엔드포인트.
 * 다운스트림 서비스 장애 시 gateway가 forward:/fallback/{module}으로 라우팅한다.
 * 응답 구조는 다운스트림 ApiResponse(data/message 필드)와 동일하게 맞춰
 * 클라이언트가 일관된 형식으로 처리할 수 있도록 한다.
 */
@RestController
public class FallbackController {

    @RequestMapping("/fallback/{module}")
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, Object>> fallback(@PathVariable String module) {
        String message = module + " 서비스가 일시적으로 응답하지 않습니다. 잠시 후 다시 시도해주세요.";
        Map<String, Object> body = Map.of("data", Map.of(), "message", message);
        return Mono.just(body);
    }
}
