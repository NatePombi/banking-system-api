package com.nate.bankingsystemapi.model;

import com.nate.bankingsystemapi.exception.CurrencyCodeMismatchException;

public enum CurrencyCode {
    ZAR,USD,EUR;


    public static CurrencyCode getCurrencyCode(String currencyCode) {

        try{
            return CurrencyCode.valueOf(currencyCode.toUpperCase().trim());
        }

        catch (IllegalArgumentException e){
            throw new CurrencyCodeMismatchException("Invalid currency code: " + currencyCode);
        }
    }
}
