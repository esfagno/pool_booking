package com.poolapp.pool.exception;

public class NoFreePlacesException extends RuntimeException {

    public NoFreePlacesException(String message) {
        super(message);
    }
}
