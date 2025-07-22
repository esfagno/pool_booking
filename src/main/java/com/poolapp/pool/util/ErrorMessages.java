package com.poolapp.pool.util;

public final class ErrorMessages {
    public static final String POOL_NOT_FOUND = "Pool not found:";
    public static final String SCHEDULE_NOT_FOUND = "Schedule not found:";
    public static final String DAY_OF_WEEK_NULL = "DayOfWeek cannot be null";
    public static final String BOOKING_NOT_FOUND = "Booking not found:";
    public static final String USER_NOT_FOUND = "User not found:";
    public static final String SESSION_NOT_FOUND = "Session not found:";
    public static final String NO_FREE_PLACES = "No free places found:";
    public static final String WRONG_STATUS = "Cannot delete booking. Current status:";
    public static final String ALREADY_ACTIVE = "User already has an active booking";
    public static final String SUBSCRIPTION_NOT_FOUND = "Subscription type not found: ";
    public static final String USER_SUBSCRIPTION_NOT_FOUND = "User subscription not found: ";
    public static final String USER_SUBSCRIPTION_EXPIRED = "User subscription expired: ";
    public static final String EMAIL_IS_TAKEN = "Email is taken:";
    public static final String ROLE_NOT_FOUND = "Role not found:";
    public static final String FORBIDDEN_OPERATION = "Forbidden operation:";

    private ErrorMessages() {
    }
}
