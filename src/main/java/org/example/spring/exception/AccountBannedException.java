package org.example.spring.exception;

public class AccountBannedException extends RuntimeException {

    public AccountBannedException(String message) {
        super(message);
    }
}
