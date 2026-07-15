package com.nate.bankingsystemapi.service.ledger;

import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;

import java.math.BigDecimal;

public interface ILedgerService {
    void recordCredit(Transactions transactions, Account account, BigDecimal amount);
    void recordDebit(Transactions transactions, Account account, BigDecimal amount);

}
