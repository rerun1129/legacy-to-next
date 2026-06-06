package com.freightos.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class FmsException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public FmsException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }

    public FmsException(HttpStatus status, String message) {
        this(status, status.name(), message);
    }

    public static FmsException notFound(String message) {
        return new FmsException(HttpStatus.NOT_FOUND, message);
    }

    public static FmsException badRequest(String message) {
        return new FmsException(HttpStatus.BAD_REQUEST, message);
    }

    public static FmsException conflict(String errorCode, String message) {
        return new FmsException(HttpStatus.CONFLICT, errorCode, message);
    }
}
