package com.poolapp.pool.util.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ErrorMessages {
    public static final String POOL_NOT_FOUND = "Pool with ID %s not found";
    public static final String SCHEDULE_NOT_FOUND = "Schedule with ID %s not found";
    public static final String DAY_OF_WEEK_NULL = "DayOfWeek cannot be null. A valid day must be provided";
    public static final String BOOKING_NOT_FOUND = "Booking with ID %s not found";
    public static final String USER_NOT_FOUND = "User with ID %s not found";
    public static final String SESSION_NOT_FOUND = "Session with ID %s not found";
    public static final String NO_FREE_PLACES = "No free places available for pool ID %s at the requested time";
    public static final String WRONG_STATUS = "Cannot delete booking. Current status is '%s'";
    public static final String ALREADY_ACTIVE = "User with ID %s already has an active booking";
    public static final String SUBSCRIPTION_NOT_FOUND = "Subscription type '%s' not found";
    public static final String USER_SUBSCRIPTION_NOT_FOUND = "User subscription with ID %s not found";
    public static final String USER_SUBSCRIPTION_EXPIRED = "User subscription has expired: %s";
    public static final String EMAIL_IS_TAKEN = "Email '%s' is already registered";
    public static final String ROLE_NOT_FOUND = "Role '%s' not found";
    public static final String REFRESH_TOKEN = "Refresh token is invalid or expired";

}
