package com.poolapp.pool.exception;


import com.poolapp.pool.util.exception.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ForbiddenOperationException extends RuntimeException {
    private final ApiErrorCode errorCode;

    public ForbiddenOperationException(ApiErrorCode errorCode) {
        super(String.format("Forbidden operation: %s", errorCode.getCode()));
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.FORBIDDEN;
    }
}
