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
                acc.getUser() !=null ? acc.getUser().getId(): null
        );
    }

    public static Account toEntity(AccountDto accDto, User user){
        if(accDto == null){
            return null;
        }

        return new Account(
                accDto.getId(),
                accDto.getAccountNum(),
                accDto.getBalance(),
                accDto.getCurrency(),
                user
        );
    }
}
