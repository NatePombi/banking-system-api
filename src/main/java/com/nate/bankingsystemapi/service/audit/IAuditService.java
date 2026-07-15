package com.nate.bankingsystemapi.service.audit;

import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.user.entity.User;

import java.math.BigDecimal;

public interface IAuditService {
    void logTransfer(Transactions transactions, User user, Account from, Account to, BigDecimal amount);
}
