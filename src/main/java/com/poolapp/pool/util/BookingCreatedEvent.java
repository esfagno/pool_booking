package com.poolapp.pool.util;

import com.poolapp.pool.dto.SessionDTO;

public record BookingCreatedEvent(String userEmail, SessionDTO sessionDTO) {
}

