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
}
