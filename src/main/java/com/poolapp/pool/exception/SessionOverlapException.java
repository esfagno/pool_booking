package com.poolapp.pool.exception;


import com.poolapp.pool.util.exception.ApiErrorCode;
import org.springframework.http.HttpStatus;

public class SessionOverlapException extends RuntimeException {
    private final ApiErrorCode errorCode;
    private final String detail;

    public SessionOverlapException(String detail) {
        super(detail);
        this.errorCode = ApiErrorCode.TIME_CONFLICT;
        this.detail = detail;
    }

    @Override
    public String getMessage() {
        return errorCode.getCode() + (detail != null ? ": " + detail : "");
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}

