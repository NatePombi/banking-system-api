package com.nate.bankingsystemapi.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found.");
    }
    public UserNotFoundException() {
        super("User not found.");
    }


}
