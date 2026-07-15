package com.nate.bankingsystemapi.service.account;

import com.nate.bankingsystemapi.dto.account.AccountDto;
import com.nate.bankingsystemapi.dto.account.PostAccountDto;
import org.springframework.data.domain.Page;

public interface IAccountService {
    AccountDto createAccount(PostAccountDto postAccountDto, String username);
    AccountDto getAccountById(Long id, String username);
    AccountDto getAccountByAccountNumber(Long accNum, String username);
    Page<AccountDto> getAllUserAccount(String username,int page, int size, String sortBy, String direction);
    Page<AccountDto> adminGetAllUserAccount(String username,int page, int size, String sortBy, String direction);
    AccountDto adminGetAccountById(Long id, String username);
    AccountDto adminGetAccountByAccountNumber(Long accNum, String username);
}
