package com.poolapp.pool.util.exception;

import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ApiErrorResponse {
    private final String errorId = UUID.randomUUID().toString();
    private final LocalDateTime timestamp = LocalDateTime.now();
    private final int status;
    private final String errorCode;
    private final String path;
    private final Object details;

    public ApiErrorResponse(HttpStatus status, String errorCode, String path, Object details) {
        this.status = status.value();
        this.errorCode = errorCode;
        this.path = path;
        this.details = details;
    }
}