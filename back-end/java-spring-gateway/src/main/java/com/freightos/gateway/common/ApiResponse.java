package com.freightos.gateway.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 응답 래퍼 — admin ApiResponse와 byte-수준으로 동일한 필드 구조(data/message).
 * 게이트웨이 로컬 엔드포인트(refresh/logout)에서만 사용한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private String message;

    public static <T> ApiResponse<T> of(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.data = data;
        response.message = message;
        return response;
    }

    public static ApiResponse<Void> ok(String message) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.message = message;
        return response;
    }
}
