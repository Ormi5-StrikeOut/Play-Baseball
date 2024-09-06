package org.example.spring.exception;

import org.example.spring.constant.ErrorCode;

public class MessageException extends RuntimeException {
    private ErrorCode errorCode;

    public MessageException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
