package com.innowise.userservice.exception;

public class AccountIsNotActivatedException extends RuntimeException {
    public AccountIsNotActivatedException(String message) {
        super(message);
    }
}
