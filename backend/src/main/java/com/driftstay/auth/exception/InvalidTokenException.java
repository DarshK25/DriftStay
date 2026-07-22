package com.driftstay.auth.exception;

import com.driftstay.common.exception.UnauthorizedException;

public class InvalidTokenException extends UnauthorizedException {
    public InvalidTokenException(String message) {
        super("INVALID_TOKEN", message);
    }

    public InvalidTokenException(String message, Throwable cause) {
        super("INVALID_TOKEN", message);
        initCause(cause);
    }
}
