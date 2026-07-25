package com.driftstay.exception;

import java.time.LocalDate;

public class BookingConflictException extends RuntimeException {

    private final Long roomId;
    private final LocalDate checkIn;
    private final LocalDate checkOut;

    public BookingConflictException(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        super(buildMessage(roomId, checkIn, checkOut));
        this.roomId = roomId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public BookingConflictException(String message) {
        super(message);
        this.roomId = null;
        this.checkIn = null;
        this.checkOut = null;
    }

    private static String buildMessage(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        return String.format(
                "Room %d is not available from %s to %s. The dates overlap with an existing booking.",
                roomId, checkIn, checkOut
        );
    }

    public Long getRoomId() {
        return roomId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }
}
