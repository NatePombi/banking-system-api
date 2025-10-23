package com.nate.bankingsystemapi.mapper;

import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.model.Transaction;

public class TransactionMapper {

    public static TransactionDto toDto(Transaction transaction){

        if(transaction == null){
            return null;
        }

        return new TransactionDto(
                transaction.getId(),
                transaction.getAmount(),
                transaction.getFromAccount().getId(),
                transaction.getToAccount().getId(),
                transaction.getStatus()
        );
    }
}
