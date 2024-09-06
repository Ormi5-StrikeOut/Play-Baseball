package org.example.spring.exception;

public class AccountDeletedException extends RuntimeException {

    public AccountDeletedException(String message) {
        super(message);
    }
}
