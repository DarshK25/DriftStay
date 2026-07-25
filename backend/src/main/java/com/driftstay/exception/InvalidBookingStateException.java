package com.driftstay.exception;

import com.driftstay.common.enums.BookingStatus;

public class InvalidBookingStateException extends RuntimeException {

    private final BookingStatus currentState;
    private final BookingStatus targetState;
    private final String bookingReference;

    public InvalidBookingStateException(BookingStatus currentState, BookingStatus targetState, String bookingReference) {
        super(buildMessage(currentState, targetState, bookingReference));
        this.currentState = currentState;
        this.targetState = targetState;
        this.bookingReference = bookingReference;
    }

    public InvalidBookingStateException(String message) {
        super(message);
        this.currentState = null;
        this.targetState = null;
        this.bookingReference = null;
    }

    private static String buildMessage(BookingStatus currentState, BookingStatus targetState, String bookingReference) {
        return String.format(
                "Invalid booking state transition: Cannot transition booking %s from %s to %s.",
                bookingReference, currentState, targetState
        );
    }

    public BookingStatus getCurrentState() {
        return currentState;
    }

    public BookingStatus getTargetState() {
        return targetState;
    }

    public String getBookingReference() {
        return bookingReference;
    }
}
