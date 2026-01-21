package com.nate.bankingsystemapi.mapper;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.model.Account;
import com.nate.bankingsystemapi.model.User;

public class AccountMapper {

    public static AccountDto toDto(Account acc){
        if(acc == null){
            return null;
        }

        return new AccountDto(
                acc.getId(),
                acc.getAccountNum(),
                acc.getBalance(),
                acc.getCurrency(),
                acc.getUserId()
        );
    }

    public static Account toEntity(AccountDto accDto, Long userId){
        if(accDto == null){
            return null;
        }

        Account acc = new Account(userId);
        acc.changeBalance(accDto.getBalance());
        acc.changeCurrency(accDto.getCurrency());

        return acc;
    }
}
