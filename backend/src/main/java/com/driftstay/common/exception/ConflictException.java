package com.driftstay.common.exception;

public class ConflictException extends BusinessException {
    public ConflictException(String message) {
        super(ErrorCode.USER_ALREADY_EXISTS, message);
    }
}
