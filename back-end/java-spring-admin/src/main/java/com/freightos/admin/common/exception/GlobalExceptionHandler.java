package com.freightos.admin.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
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
 *   "type":   "https://admin.freightos.com/errors/NOT_FOUND",
 *   "title":  "Resource Not Found",
 *   "status": 404,
 *   "detail": "...",
 *   "instance": "/api/admin/..."
 * }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String TYPE_BASE = "https://admin.freightos.com/errors/";

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ProblemDetail> handleApplicationException(ApplicationException ex, WebRequest request) {
        log.warn("ApplicationException: [{}] {}", ex.getErrorCode(), ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(ex.getStatus());
        pd.setType(URI.create(TYPE_BASE + ex.getErrorCode()));
        pd.setTitle(ex.getStatus().getReasonPhrase());
        pd.setDetail(ex.getMessage());
        pd.setProperty("errorCode", ex.getErrorCode());
        return ResponseEntity.status(ex.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetail> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("DataIntegrityViolationException: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        pd.setType(URI.create(TYPE_BASE + "DATA_INTEGRITY_VIOLATION"));
        pd.setTitle("Data Integrity Violation");
        pd.setDetail("요청이 데이터 무결성 제약 조건을 위반했습니다.");
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        log.warn("AccessDeniedException: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        pd.setType(URI.create(TYPE_BASE + "FORBIDDEN"));
        pd.setTitle("Forbidden");
        pd.setDetail("해당 리소스에 대한 접근 권한이 없습니다.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(pd);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        log.warn("AuthenticationException: {}", ex.getMessage());
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        pd.setType(URI.create(TYPE_BASE + "UNAUTHORIZED"));
        pd.setTitle("Unauthorized");
        pd.setDetail("인증이 필요합니다.");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
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
