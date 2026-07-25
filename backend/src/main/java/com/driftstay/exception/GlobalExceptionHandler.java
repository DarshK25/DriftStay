package com.driftstay.exception;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * RFC 7807 Problem Details compliant exception handler.
 * Returns structured error responses instead of plain messages.
 *
 * Response format:
 * {
 *     "type": "https://driftstay.com/errors/booking-conflict",
 *     "title": "Booking Conflict",
 *     "status": 409,
 *     "detail": "Room 5 is not available...",
 *     "instance": "/api/bookings",
 *     "timestamp": "2026-07-23T10:30:00Z",
 *     "traceId": "a1b2c3d4"
 * }
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingConflictException.class)
    public ProblemDetail handleBookingConflict(BookingConflictException ex, WebRequest request) {
        log.warn("Booking conflict: {}", ex.getMessage());
        ProblemDetail problem = buildProblem(HttpStatus.CONFLICT, "Booking Conflict", ex.getMessage(), request);
        problem.setType(URI.create("https://driftstay.com/errors/booking-conflict"));
        return problem;
    }

    @ExceptionHandler(InvalidBookingStateException.class)
    public ProblemDetail handleInvalidBookingState(InvalidBookingStateException ex, WebRequest request) {
        log.warn("Invalid booking state: {}", ex.getMessage());
        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, "Invalid Booking State", ex.getMessage(), request);
        problem.setType(URI.create("https://driftstay.com/errors/invalid-booking-state"));
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        log.warn("Invalid request: {}", ex.getMessage());
        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), request);
        problem.setType(URI.create("https://driftstay.com/errors/bad-request"));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
        log.warn("Validation errors: {}", errors);

        ProblemDetail problem = buildProblem(HttpStatus.BAD_REQUEST, "Validation Failed",
                "Request validation failed for " + errors.size() + " field(s)", request);
        problem.setType(URI.create("https://driftstay.com/errors/validation"));
        problem.setProperty("fields", errors);
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnknown(Exception ex, WebRequest request) {
        log.error("Unhandled exception", ex);
        ProblemDetail problem = buildProblem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error", "An unexpected error occurred", request);
        problem.setType(URI.create("https://driftstay.com/errors/internal-error"));
        return problem;
    }

    private ProblemDetail buildProblem(HttpStatus status, String title, String detail, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getDescription(false).replace("uri=", "")));

        // Add request tracing
        String traceId = MDC.get("requestId");
        if (traceId != null) {
            problem.setProperty("traceId", traceId);
        }
        problem.setProperty("timestamp", Instant.now().toString());

        return problem;
    }
}
