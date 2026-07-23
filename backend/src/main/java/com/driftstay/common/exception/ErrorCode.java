package com.driftstay.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // General
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),

    // Auth
    USER_NOT_FOUND(HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN),
    PASSWORD_RESET_EXPIRED(HttpStatus.BAD_REQUEST),
    USER_DISABLED(HttpStatus.FORBIDDEN),

    // Property
    PROPERTY_NOT_FOUND(HttpStatus.NOT_FOUND),
    SLUG_ALREADY_EXISTS(HttpStatus.CONFLICT),

    // Room
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND),
    ROOM_NOT_AVAILABLE(HttpStatus.CONFLICT),

    // Booking
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND),
    BOOKING_CANCELLED(HttpStatus.CONFLICT),
    BOOKING_ALREADY_EXISTS(HttpStatus.CONFLICT),
    DATES_UNAVAILABLE(HttpStatus.CONFLICT),

    // Payment
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND),
    PAYMENT_FAILED(HttpStatus.PAYMENT_REQUIRED),
    PAYMENT_ALREADY_PROCESSED(HttpStatus.CONFLICT),

    // Review
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
