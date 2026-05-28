package com.freightos.admin.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;

    public ApplicationException(HttpStatus status, String errorCode, String message) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }

    public ApplicationException(HttpStatus status, String message) {
        this(status, status.name(), message);
    }

    public static ApplicationException badRequest(String errorCode, String message) {
        return new ApplicationException(HttpStatus.BAD_REQUEST, errorCode, message);
    }

    public static ApplicationException notFound(String errorCode, String message) {
        return new ApplicationException(HttpStatus.NOT_FOUND, errorCode, message);
    }

    public static ApplicationException conflict(String errorCode, String message) {
        return new ApplicationException(HttpStatus.CONFLICT, errorCode, message);
    }
}
