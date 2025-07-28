package com.poolapp.pool.exception;

import com.poolapp.pool.util.exception.ApiErrorCode;

public class ModelNotFoundException extends RuntimeException {
    private final ApiErrorCode errorCode;
    private final String detail;

    public ModelNotFoundException(ApiErrorCode errorCode, String detail) {
        super(detail);
        this.errorCode = errorCode;
        this.detail = detail;
    }

    public ApiErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetail() {
        return detail;
    }
}
