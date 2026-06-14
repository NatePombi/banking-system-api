package com.nate.bankingsystemapi.exception;

public class UsernameExistsException extends RuntimeException {
    public UsernameExistsException(String message) {
        super(message);
    }
}
