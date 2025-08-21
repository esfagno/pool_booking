package com.poolapp.pool.util;

import com.poolapp.pool.dto.SessionDTO;
import com.poolapp.pool.dto.requestDTO.RequestSessionDTO;
import com.poolapp.pool.exception.ModelNotFoundException;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.model.User;
import com.poolapp.pool.service.SessionService;
import com.poolapp.pool.service.UserService;
import com.poolapp.pool.util.exception.ApiErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingContextBuilder {

    private final UserService userService;
    private final SessionService sessionService;

    public BookingContext build(String userEmail, SessionDTO sessionDTO) {
        User user = userService.findUserByEmail(userEmail)
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND, "User not found: " + userEmail));

        Session session = sessionService.getSessionByPoolNameAndStartTime(
                        sessionDTO.getPoolName(), sessionDTO.getStartTime())
                .orElseThrow(() -> new ModelNotFoundException(
                        ApiErrorCode.NOT_FOUND,
                        String.format("Session not found: pool=%s, startTime=%s",
                                sessionDTO.getPoolName(), sessionDTO.getStartTime())));

        return BookingContext.of(user, session);
    }

    public BookingContext build(String userEmail, RequestSessionDTO requestSessionDTO) {
        SessionDTO sessionDTO = SessionDTO.builder()
                .poolName(requestSessionDTO.getRequestPoolDTO().getName())
                .startTime(requestSessionDTO.getStartTime())
                .build();
        return build(userEmail, sessionDTO);
    }
}