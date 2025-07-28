package com.poolapp.pool.exception;

import com.poolapp.pool.util.exception.ApiErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


@Getter
@RequiredArgsConstructor
public class ModelNotFoundException extends RuntimeException {
    private final ApiErrorCode errorCode;
    private final String detail;


    public HttpStatus getHttpStatus() {
        return HttpStatus.NOT_FOUND;
    }
}