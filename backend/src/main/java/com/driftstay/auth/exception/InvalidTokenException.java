package com.driftstay.auth.exception;

import com.driftstay.common.exception.BusinessException;
import com.driftstay.common.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
}
