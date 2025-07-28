package com.poolapp.pool.controller;

import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.util.exception.ApiErrorCode;
import com.poolapp.pool.util.exception.ApiErrorMessages;
import com.poolapp.pool.util.exception.ApiErrorResponse;
import com.poolapp.pool.util.exception.EntityAlreadyExistsException;
import com.poolapp.pool.util.exception.ForbiddenOperationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ForbiddenOperationException.class)
    protected ResponseEntity<Object> handleForbiddenOperation(ForbiddenOperationException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        String message = ApiErrorMessages.format(ex.getReason().name());

        log.warn("FORBIDDEN[{}]: {} - {}", errorId, ex.getReason(), path);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.FORBIDDEN,
                ApiErrorCode.FORBIDDEN_OPERATION.getCode(),
                path,
                Map.of("reason", ex.getReason().name(), "message", message)
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler({EntityNotFoundException.class, ModelNotFoundException.class})
    protected ResponseEntity<Object> handleEntityNotFound(RuntimeException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        ApiErrorCode errorCode;
        String details = null;

        if (ex.getMessage() != null && ex.getMessage().contains("Pool")) {
            errorCode = ApiErrorCode.POOL_NOT_FOUND;
            details = extractIdFromMessage(ex.getMessage());
        } else if (ex.getMessage() != null && ex.getMessage().contains("Role")) {
            errorCode = ApiErrorCode.ROLE_NOT_FOUND;
            details = extractRoleFromMessage(ex.getMessage());
        } else {
            errorCode = ApiErrorCode.NOT_FOUND;
            details = ex.getMessage();
        }

        String message = ApiErrorMessages.format(errorCode.getCode(), details != null ? details : "");
        log.warn("NOT_FOUND[{}]: {} - {}", errorId, errorCode, ex.getMessage());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.NOT_FOUND,
                errorCode.getCode(),
                path,
                Map.of("details", details, "message", message)
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    protected ResponseEntity<Object> handleEntityAlreadyExists(EntityAlreadyExistsException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        ApiErrorCode errorCode;
        String details = null;

        if (ex.getMessage() != null && ex.getMessage().contains("email")) {
            errorCode = ApiErrorCode.EMAIL_TAKEN;
            details = extractEmailFromMessage(ex.getMessage());
        } else {
            errorCode = ApiErrorCode.ALREADY_EXISTS;
            details = ex.getMessage();
        }

        String message = ApiErrorMessages.format(errorCode.getCode(), details != null ? details : "");
        log.warn("ALREADY_EXISTS[{}]: {} - {}", errorId, errorCode, ex.getMessage());

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.CONFLICT,
                errorCode.getCode(),
                path,
                Map.of("details", details, "message", message)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        String message = ApiErrorMessages.getMessage(ApiErrorCode.INVALID_CREDENTIALS.getCode());

        log.warn("INVALID_CREDENTIALS[{}]: {}", errorId, path);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.UNAUTHORIZED,
                ApiErrorCode.INVALID_CREDENTIALS.getCode(),
                path,
                Map.of("message", message)
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);

        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage()
                ));

        String message = ApiErrorMessages.getMessage(ApiErrorCode.VALIDATION_ERROR.getCode());
        log.warn("VALIDATION[{}]: {} - {}", errorId, path, errors);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR.getCode(),
                path,
                Map.of(
                        "message", message,
                        "errors", errors
                )
        );

        return new ResponseEntity<>(errorResponse, headers, status);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        String errorId = UUID.randomUUID().toString();
        String path = extractPath(request);
        ApiErrorCode errorCode = ApiErrorCode.INTERNAL_ERROR;
        String message = ApiErrorMessages.getMessage(errorCode.getCode());

        log.error("INTERNAL_ERROR[{}]: {} - {}", errorId, path, ex.getMessage(), ex);

        ApiErrorResponse errorResponse = new ApiErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                errorCode.getCode(),
                path,
                Map.of(
                        "errorId", errorId,
                        "message", message
                )
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
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
            if (email.contains("@")) {
                return email;
            }
        }
        int colonIndex = message.indexOf(':');
        if (colonIndex > 0) {
            String email = message.substring(colonIndex + 1).trim();
            if (email.contains("@")) {
                return email;
            }
        }
        return null;
    }

    private String extractIdFromMessage(String message) {
        if (message == null) return null;
        int colonIndex = message.lastIndexOf(':');
        if (colonIndex > 0 && colonIndex < message.length() - 1) {
            return message.substring(colonIndex + 1).trim();
        }
        return null;
    }

    private String extractRoleFromMessage(String message) {
        if (message == null) return null;
        int start = message.indexOf('\'');
        int end = message.lastIndexOf('\'');
        if (start >= 0 && end > start) {
            return message.substring(start + 1, end);
        }
        return null;
    }
}