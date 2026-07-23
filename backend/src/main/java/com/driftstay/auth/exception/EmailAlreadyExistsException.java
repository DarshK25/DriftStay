package com.driftstay.auth.exception;

import com.driftstay.common.exception.BusinessException;
import com.driftstay.common.exception.ErrorCode;

public class EmailAlreadyExistsException extends BusinessException {
    public EmailAlreadyExistsException(String message) {
        super(ErrorCode.USER_ALREADY_EXISTS, message);
    }
}
