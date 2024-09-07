package org.example.spring.exception;

public class EmailVerificationTokenExpiredException extends RuntimeException {

    public EmailVerificationTokenExpiredException(String message) {
        super(message);
    }
}
