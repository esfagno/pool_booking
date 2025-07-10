package com.poolapp.pool.exception;

public class UserSubscriptionExpiredException extends RuntimeException {

    public UserSubscriptionExpiredException(String message) {
        super(message);
    }
}
