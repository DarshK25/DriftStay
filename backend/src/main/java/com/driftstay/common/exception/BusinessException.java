package com.driftstay.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

@Getter
public abstract class BusinessException extends RuntimeException {

    private final String code;
    private final HttpStatus status;

    protected BusinessException(String code, String message, HttpStatus status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public ProblemDetail toProblemDetail() {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(status, getMessage());
        detail.setType(URI.create("https://api.driftstay.com/errors/" + code.toLowerCase()));
        detail.setTitle(status.getReasonPhrase());
        detail.setProperty("code", code);
        return detail;
    }
}
