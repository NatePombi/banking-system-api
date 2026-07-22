package com.nate.bankingsystemapi.exception;

public class InsufficientAmountException extends RuntimeException {
    public InsufficientAmountException(String message) {
        super(message);
    }
}
