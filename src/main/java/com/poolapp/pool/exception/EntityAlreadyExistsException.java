package com.poolapp.pool.exception;

import com.poolapp.pool.util.exception.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EntityAlreadyExistsException extends RuntimeException {
    private final ApiErrorCode errorCode;
    private final String entity;
    private final String identifier;

    public EntityAlreadyExistsException(ApiErrorCode errorCode, String entity, String identifier) {
        super(String.format("%s already exists: %s", entity, identifier));
        this.errorCode = errorCode;
        this.entity = entity;
        this.identifier = identifier;
    }

    public EntityAlreadyExistsException(String message) {
        super(message);
        this.errorCode = ApiErrorCode.ALREADY_EXISTS;
        this.entity = "Entity";
        this.identifier = message;
    }

    public HttpStatus getHttpStatus() {
        return HttpStatus.CONFLICT;
    }
}