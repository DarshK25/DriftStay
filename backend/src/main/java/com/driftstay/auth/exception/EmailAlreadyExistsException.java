package com.driftstay.auth.exception;

import com.driftstay.common.exception.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("EMAIL_EXISTS", "Email already registered: " + email);
    }
}
