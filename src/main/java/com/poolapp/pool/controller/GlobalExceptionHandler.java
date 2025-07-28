package com.poolapp.pool.controller;

import com.poolapp.pool.exception.EntityAlreadyExistsException;
import com.poolapp.pool.exception.ForbiddenOperationException;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.util.exception.ApiErrorCode;
import com.poolapp.pool.util.exception.ApiErrorMessages;
import com.poolapp.pool.util.exception.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private ResponseEntity<Object> createResponse(HttpStatus status, String errorCode, String path, Map<String, Object> details) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(status, errorCode, path, details));
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    protected ResponseEntity<Object> handleForbidden(ForbiddenOperationException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        String message = ApiErrorMessages.getMessage(ex.getErrorCode().getCode());

        log.warn("FORBIDDEN[{}]: {} - {}", errorId, ex.getErrorCode().getCode(), path);

        return createResponse(ex.getErrorCode().getHttpStatus(), ex.getErrorCode().getCode(), path, Map.of("reason", ex.getErrorCode().getCode(), "message", message));
    }

    @ExceptionHandler(ModelNotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(ModelNotFoundException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        String message = ApiErrorMessages.format(ex.getErrorCode().getCode(), ex.getDetail());

        log.warn("NOT_FOUND[{}]: {} - {}", errorId, ex.getErrorCode().getCode(), path);

        return createResponse(HttpStatus.NOT_FOUND, ex.getErrorCode().getCode(), path, Map.of("details", ex.getDetail(), "message", message));
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    protected ResponseEntity<Object> handleConflict(EntityAlreadyExistsException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        ApiErrorCode errorCode = ApiErrorCode.ALREADY_EXISTS;
        String details = ex.getMessage();

        if (ex.getMessage() != null && ex.getMessage().contains("email")) {
            errorCode = ApiErrorCode.EMAIL_TAKEN;
            details = extractEmailFromMessage(ex.getMessage());
        }

        String message = ApiErrorMessages.format(errorCode.getCode(), details);
        log.warn("ALREADY_EXISTS[{}]: {} - {}", errorId, errorCode.getCode(), ex.getMessage());

        return createResponse(HttpStatus.CONFLICT, errorCode.getCode(), path, Map.of("details", details, "message", message));
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleUnauthorized(BadCredentialsException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        String message = ApiErrorMessages.getMessage(ApiErrorCode.INVALID_CREDENTIALS.getCode());

        log.warn("INVALID_CREDENTIALS[{}]: {}", errorId, path);

        return createResponse(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS.getCode(), path, Map.of("message", message));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream().collect(HashMap::new, (map, error) -> map.put(error.getField(), error.getDefaultMessage()), HashMap::putAll);

        String message = ApiErrorMessages.getMessage(ApiErrorCode.VALIDATION_ERROR.getCode());
        log.warn("VALIDATION[{}]: {} - {}", errorId, path, errors);

        return createResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR.getCode(), path, Map.of("message", message, "errors", errors));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGlobal(Exception ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        String message = ApiErrorMessages.getMessage(ApiErrorCode.INTERNAL_ERROR.getCode());

        log.error("INTERNAL_ERROR[{}]: {} - {}", errorId, path, ex.getMessage(), ex);

        return createResponse(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR.getCode(), path, Map.of("errorId", errorId, "message", message));
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private String extractEmailFromMessage(String message) {
        if (message == null) return null;
        int start = message.indexOf('\'');
        int end = message.lastIndexOf('\'');
        if (start >= 0 && end > start) {
            String email = message.substring(start + 1, end);
            if (email.contains("@")) return email;
        }
        int colonIndex = message.indexOf(':');
        if (colonIndex > 0) {
            String email = message.substring(colonIndex + 1).trim();
            if (email.contains("@")) return email;
        }
        return null;
    }
}