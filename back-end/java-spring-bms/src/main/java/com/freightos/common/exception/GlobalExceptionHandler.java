package com.freightos.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * RFC 7807 Problem Details (Spring Framework 6+ 내장 ProblemDetail 활용).
 * 에러 응답 형식:
 * {
 *   "type":   "https://fms.freightos.com/errors/NOT_FOUND",
 *   "title":  "Resource Not Found",
 *   "status": 404,
 *   "detail": "HouseBl not found: abc123",
 *   "instance": "/api/v1/house-bl/abc123"
 * }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TYPE_BASE = "https://fms.freightos.com/errors/";

    @ExceptionHandler(FmsException.class)
    public ResponseEntity<ProblemDetail> handleFmsException(FmsException ex, WebRequest request) {
        log.warn("FmsException: [{}] {}", ex.getErrorCode(), ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setType(URI.create(TYPE_BASE + ex.getErrorCode()));
        pd.setTitle(ex.getStatus().getReasonPhrase());
        pd.setDetail(ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> "[%s] %s".formatted(fe.getField(), fe.getDefaultMessage()))
                .collect(Collectors.joining("\n"));

        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setType(URI.create(TYPE_BASE + "VALIDATION_FAILED"));
        pd.setTitle("Validation Failed");
        pd.setDetail(detail);
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("ConstraintViolationException: {}", ex.getMessage());
        String detail = ex.getConstraintViolations().stream()
                .map(v -> {
                    String path = v.getPropertyPath().toString();
                    int dotIdx = path.lastIndexOf('.');
                    String field = dotIdx >= 0 ? path.substring(dotIdx + 1) : path;
                    return "[%s] %s".formatted(field, v.getMessage());
                })
                .collect(Collectors.joining("\n"));
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create(TYPE_BASE + "VALIDATION_FAILED"));
        pd.setTitle("Validation Failed");
        pd.setDetail(detail);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn("IllegalArgumentException: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create(TYPE_BASE + "INVALID_ARGUMENT"));
        pd.setTitle("Bad Request");
        pd.setDetail(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setType(URI.create(TYPE_BASE + "INTERNAL_ERROR"));
        pd.setTitle("Internal Server Error");
        pd.setDetail("An unexpected error occurred. Please contact support.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }
}
