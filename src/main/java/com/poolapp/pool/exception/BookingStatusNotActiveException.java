package com.poolapp.pool.exception;

public class BookingStatusNotActiveException extends RuntimeException {

    public BookingStatusNotActiveException(String message) {
        super(message);
    }
}
