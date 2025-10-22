package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import org.springframework.data.domain.Page;

public interface IAccountService {
    AccountDto createAccount(PostAccountDto postAccountDto, String username);
    AccountDto getAccountById(Long id, String username);
    Page<AccountDto> getAllUserAccount(String username,int page, int size, String sortBy, String direction);
}
