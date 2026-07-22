package com.nate.bankingsystemapi.exception;

public class NoAmountException extends RuntimeException {
    public NoAmountException(String message) {
        super(message);
    }
}
