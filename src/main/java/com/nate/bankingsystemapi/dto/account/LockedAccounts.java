package com.nate.bankingsystemapi.dto.account;

import com.nate.bankingsystemapi.model.account.entity.Account;


public record LockedAccounts(
        Account fromAccount,
        Account toAccount
) {


}
