package com.poolapp.pool.exception;

import com.poolapp.pool.util.exception.ApiErrorCode;
import com.poolapp.pool.util.exception.ApiErrorMessages;
import com.poolapp.pool.util.exception.ApiErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return buildAndLogError(request, HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "ACCESS_DENIED", ex, Map.of());
    }

    @ExceptionHandler(ModelNotFoundException.class)
    protected ResponseEntity<Object> handleNotFound(ModelNotFoundException ex, WebRequest request) {
        String details = ex.getDetail();
        return buildAndLogError(request, HttpStatus.NOT_FOUND, ex.getErrorCode(), "NOT_FOUND", ex, Map.of("details", details));
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleUnauthorized(BadCredentialsException ex, WebRequest request) {
        return buildAndLogError(request, HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS, "INVALID_CREDENTIALS", ex, Map.of());
    }

    @ExceptionHandler(SessionOverlapException.class)
    protected ResponseEntity<Object> handleTimeConflictException(SessionOverlapException ex, WebRequest request) {
        return buildAndLogError(request, HttpStatus.CONFLICT, ApiErrorCode.TIME_CONFLICT, "TIME_CONFLICT", ex, Map.of());
    }

    @ExceptionHandler(BookingStatusNotActiveException.class)
    protected ResponseEntity<Object> handleBookingStatusNotActive(BookingStatusNotActiveException ex, WebRequest request) {
        Map<String, Object> details = Map.of("message", ex.getMessage());
        return buildAndLogError(request, HttpStatus.CONFLICT, ApiErrorCode.BUSINESS_RULE_VIOLATION, "BUSINESS_RULE", ex, details);
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    protected ResponseEntity<Object> handleConflict(EntityAlreadyExistsException ex, WebRequest request) {
        ApiErrorCode errorCode = ex.getErrorCode();
        Map<String, Object> details = Map.of("message", "Email is already taken", "reason", ex.getMessage());


        return buildAndLogError(request, errorCode.getHttpStatus(), errorCode, "ALREADY_EXISTS", ex, details);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream().collect(HashMap::new, (map, error) -> map.put(error.getField(), error.getDefaultMessage()), HashMap::putAll);

        Map<String, Object> extraDetails = Map.of("errors", fieldErrors);
        return buildAndLogError(request, HttpStatus.BAD_REQUEST, ApiErrorCode.VALIDATION_ERROR, "VALIDATION", ex, extraDetails);
    }


    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleGlobal(Exception ex, WebRequest request) {
        log.error("Unexpected error", ex);
        return buildAndLogError(request, HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR, "INTERNAL_ERROR", ex, Map.of());
    }


    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private ResponseEntity<Object> buildAndLogError(WebRequest request, HttpStatus status, ApiErrorCode errorCode, String logPrefix, Throwable cause, Map<String, Object> extraDetails) {
        String path = extractPath(request);
        String message = ApiErrorMessages.getMessage(errorCode.getCode());
        Map<String, Object> details = new HashMap<>(extraDetails);
        details.put("message", message);

        ApiErrorResponse response = new ApiErrorResponse(status, errorCode.getCode(), path, details);

        log.error("{}[{}]: {} - Cause: {}", logPrefix, response.getErrorId(), path, cause.getMessage());

        return ResponseEntity.status(status).body(response);
    }
}