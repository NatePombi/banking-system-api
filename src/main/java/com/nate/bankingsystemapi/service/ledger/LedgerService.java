package com.nate.bankingsystemapi.service.ledger;

import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.ledger.entity.LedgerEntry;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.repository.LedgerEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class LedgerService implements ILedgerService{

    private final LedgerEntryRepository ledgerEntryRepository;

    @Override
    public void recordCredit(Transactions transactions, Account account, BigDecimal amount) {
        LedgerEntry ledgerEntry = LedgerEntry.credit(transactions, account, amount);

        ledgerEntryRepository.save(ledgerEntry);
    }

    @Override
    public void recordDebit(Transactions transactions, Account account, BigDecimal amount) {
        LedgerEntry ledgerEntry = LedgerEntry.debit(transactions, account, amount);

        ledgerEntryRepository.save(ledgerEntry);
    }
}
