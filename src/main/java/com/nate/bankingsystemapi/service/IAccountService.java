package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import org.springframework.data.domain.Page;

public interface IAccountService {
    AccountDto createAccount(PostAccountDto postAccountDto, Long userId);
    AccountDto getAccountById(Long id, Long userId);
    AccountDto getAccountByAccountNumber(Long accNum, Long userId);
    Page<AccountDto> getAllUserAccount(Long userId,int page, int size, String sortBy, String direction);
}
