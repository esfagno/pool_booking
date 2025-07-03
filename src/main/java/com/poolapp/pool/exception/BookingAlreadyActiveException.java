package com.poolapp.pool.exception;

public class BookingAlreadyActiveException extends RuntimeException {

    public BookingAlreadyActiveException(String message) {
        super(message);
    }
}