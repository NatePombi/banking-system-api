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
                acc.getBalanceCents(),
                acc.getCurrency(),
                acc.getUser() !=null ? acc.getUser().getId(): null,
                acc.getVersion()
        );
    }

    public static Account toEntity(AccountDto accDto, User user){
        if(accDto == null){
            return null;
        }

        return new Account(
                accDto.getId(),
                accDto.getBalanceCent(),
                accDto.getCurrency(),
                user,
                accDto.getInteger()
        );
    }
}
