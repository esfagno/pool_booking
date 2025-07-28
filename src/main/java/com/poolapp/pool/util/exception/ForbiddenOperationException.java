package com.poolapp.pool.util.exception;


public class ForbiddenOperationException extends RuntimeException {

    private final ForbiddenReason reason;

    public ForbiddenOperationException(ForbiddenReason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public ForbiddenReason getReason() {
        return reason;
    }
}

