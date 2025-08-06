package com.poolapp.pool.util.exception;

import org.springframework.http.HttpStatus;

public enum ApiErrorCode {
    UNAUTHORIZED("UNAUTHORIZED"),
    FORBIDDEN("FORBIDDEN"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS"),

    NOT_FOUND("NOT_FOUND"),
    POOL_NOT_FOUND("POOL_NOT_FOUND"),
    ROLE_NOT_FOUND("ROLE_NOT_FOUND"),

    ALREADY_EXISTS("ALREADY_EXISTS"),
    EMAIL_TAKEN("EMAIL_TAKEN"),

    VALIDATION_ERROR("VALIDATION_ERROR"),

    TIME_CONFLICT("TIME_CONFLICT"),

    INTERNAL_ERROR("INTERNAL_ERROR"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE");

    private final String code;
    private final HttpStatus httpStatus;

    ApiErrorCode(String code) {
        this(code, HttpStatus.FORBIDDEN);
    }

    ApiErrorCode(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}