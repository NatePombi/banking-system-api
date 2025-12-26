package com.nate.bankingsystemapi.mapper;

import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.model.Transactions;

public class TransactionMapper {

    public static TransactionDto toDto(Transactions transactions){

        if(transactions == null){
            return null;
        }

        return new TransactionDto(
                transactions.getId(),
                transactions.getAmount(),
                transactions.getFromAccount().getId(),
                transactions.getToAccount().getId(),
                transactions.getStatus()
        );
    }
}
