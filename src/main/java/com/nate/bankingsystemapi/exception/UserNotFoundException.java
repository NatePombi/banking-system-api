package com.nate.bankingsystemapi.exception;


public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("User " + username + " not found.");
    }
    public UserNotFoundException() {
        super("User not found.");
    }


}
