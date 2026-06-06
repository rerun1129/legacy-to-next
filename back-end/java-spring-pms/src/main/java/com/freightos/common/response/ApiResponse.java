package com.freightos.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통 응답 래퍼.
 * 성공 시: { "data": {...}, "message": "..." }
 * 에러 시: RFC 7807 ProblemDetail (GlobalExceptionHandler 참고)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;
    private String message;

    public static <T> ApiResponse<T> of(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> of(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.data = data;
        response.message = message;
        return response;
    }

    public static ApiResponse<Void> ok() {
        return of(null, "OK");
    }

    public static ApiResponse<Void> ok(String message) {
        return of(null, message);
    }
}
