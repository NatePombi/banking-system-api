package com.nate.bankingsystemapi.mapper;

import com.nate.bankingsystemapi.dto.transaction.TransactionDto;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;

import java.math.BigDecimal;

public class TransactionMapper {

    public static TransactionDto toDto(Transactions transactions, Account fromAccount, Account toAccount, BigDecimal amount){

        if(transactions == null){
            return null;
        }

        return new TransactionDto(
                transactions.getId(),
                amount,
                fromAccount.getAccountNum(),
                toAccount.getAccountNum(),
                transactions.getStatus()
        );
    }
}
