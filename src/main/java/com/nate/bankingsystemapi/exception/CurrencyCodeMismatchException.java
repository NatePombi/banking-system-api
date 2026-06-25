package com.nate.bankingsystemapi.exception;

public class CurrencyCodeMismatchException extends RuntimeException {
    public CurrencyCodeMismatchException(String message) {
        super(message);
    }
}
