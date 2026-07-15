package com.nate.bankingsystemapi.exception;

import com.nate.bankingsystemapi.model.user.entity.User;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String username) {
        super("User " + username + " not found.");
    }
    public UserNotFoundException(User user) {
        super("User " + user.getUsername() + " not found.");
    }
    public UserNotFoundException() {
        super("User not found.");
    }


}
