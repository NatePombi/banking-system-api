package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import com.nate.bankingsystemapi.model.User;
import org.springframework.data.domain.Page;

public interface IAccountService {
    AccountDto createAccount(PostAccountDto postAccountDto, User user);
    AccountDto getAccountById(Long id, User user);
    AccountDto getAccountByAccountNumber(Long accNum, User user);
    Page<AccountDto> getAllUserAccount(User user,int page, int size, String sortBy, String direction);
}
