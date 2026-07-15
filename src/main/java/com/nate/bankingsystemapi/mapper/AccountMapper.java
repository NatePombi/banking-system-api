package com.nate.bankingsystemapi.mapper;

import com.nate.bankingsystemapi.dto.account.AccountDto;
import com.nate.bankingsystemapi.model.account.entity.Account;

public class AccountMapper {

    public static AccountDto toDto(Account acc){
        if(acc == null){
            return null;
        }

        return new AccountDto(
                acc.getId(),
                acc.getAccountNum(),
                acc.getBalance(),
                acc.getCurrency().toString(),
                acc.getUser().getId()
        );
    }

}
