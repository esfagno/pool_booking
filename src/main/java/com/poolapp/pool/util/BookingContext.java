package com.poolapp.pool.util;

import com.poolapp.pool.model.BookingId;
import com.poolapp.pool.model.Session;
import com.poolapp.pool.model.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BookingContext {

    private final User user;
    private final Session session;
    private final BookingId bookingId;

    public static BookingContext of(User user, Session session) {
        BookingId id = new BookingId(user.getId(), session.getId());
        return new BookingContext(user, session, id);
    }

    public User getUser() {
        return user;
    }

    public Session getSession() {
        return session;
    }

    public BookingId getBookingId() {
        return bookingId;
    }
}

