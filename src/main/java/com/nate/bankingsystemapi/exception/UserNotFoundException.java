package com.nate.bankingsystemapi.exception;

import com.nate.bankingsystemapi.model.User;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User with id " + id + " not found.");
    }
    public UserNotFoundException(User user) {
        super("User " + user.getUsername() + " not found.");
    }
    public UserNotFoundException() {
        super("User not found.");
    }


}
