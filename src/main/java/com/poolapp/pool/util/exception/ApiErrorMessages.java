package com.poolapp.pool.util.exception;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ApiErrorMessages {
    private static final Map<String, String> messages = Stream.of(new String[][]{
            {"ROLE_CHANGE_NOT_ALLOWED", "You are not allowed to change roles."},
            {"ROLE_CHANGE_NOT_ALLOWED_OWN", "You are not allowed to change your own role."},
            {"MODIFY_OTHER_USER_NOT_ALLOWED", "You cannot modify other users."},
            {"FORBIDDEN_OPERATION", "Forbidden operation: %s"},
            {"EMAIL_IS_TAKEN", "Email '%s' is already registered"},
            {"INVALID_CREDENTIALS", "Invalid email or password"},
            {"POOL_NOT_FOUND", "Pool with ID %s not found"},
            {"ROLE_NOT_FOUND", "Role '%s' not found"},
            {"SUBSCRIPTION_NOT_FOUND", "Subscription type '%s' not found"},
            {"USER_SUBSCRIPTION_NOT_FOUND", "User subscription '%s' not found"},
            {"VALIDATION_ERROR", "Validation failed"},
            {"INVALID_EMAIL_FORMAT", "Invalid email format"},
            {"PASSWORD_TOO_SHORT", "Password must be at least %d characters"},
            {"INTERNAL_ERROR", "An unexpected error occurred. Please try again later."},
            {"SERVICE_UNAVAILABLE", "The service is temporarily unavailable. Please try again later."}
    }).collect(Collectors.toMap(data -> data[0], data -> data[1]));

    private ApiErrorMessages() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String getMessage(String code) {
        return messages.getOrDefault(code, "Unexpected error occurred.");
    }

    public static String format(String code, Object... args) {
        String message = messages.getOrDefault(code, "Unexpected error occurred.");
        try {
            return String.format(message, args);
        } catch (Exception e) {
            return message + " [Parameter formatting error]";
        }
    }
}