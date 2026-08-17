package com.nate.bankingsystemapi.service.audit;

import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.user.entity.User;

import java.math.BigDecimal;

public interface IAuditService {
    void logTransfer(Long transactionId, String authenticatedUserUsername, Account from, Account to, BigDecimal amount);
    void logDeposit(Long transactionId, String authenticatedUserUsername, Account account, BigDecimal amount);
    void logWithdraw(Long transactionId, String authenticatedUserUsername, Account account, BigDecimal amount);


}
