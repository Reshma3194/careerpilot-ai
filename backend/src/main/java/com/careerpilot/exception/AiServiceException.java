package com.careerpilot.exception;

import org.springframework.http.HttpStatus;

public class AiServiceException extends RuntimeException {

    private final HttpStatus status;

    public AiServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public AiServiceException(String message) {
        this(message, HttpStatus.BAD_GATEWAY);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
