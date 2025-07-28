package com.poolapp.pool.util.exception;

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

    INTERNAL_ERROR("INTERNAL_ERROR"),
    SERVICE_UNAVAILABLE("SERVICE_UNAVAILABLE"),

    FORBIDDEN_OPERATION("FORBIDDEN_OPERATION");

    private final String code;

    ApiErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
