package com.nate.bankingsystemapi.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("Account with id " + id +" was not found");
    }

    public AccountNotFoundException() {
        super("Account with that account number was not found");
    }

    public AccountNotFoundException(String username) {
        super("There is no accounts owned by " + username);
    }

}
