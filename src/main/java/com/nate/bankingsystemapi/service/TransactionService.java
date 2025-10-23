package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.mapper.TransactionMapper;
import com.nate.bankingsystemapi.model.*;
import com.nate.bankingsystemapi.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository repo;
    private final AccountRepository repoA;
    private final UserRepository repoU;
    private final LedgerEntryRepository repoT;
    private final AuditLogRepository auditLogRepo;


    @Transactional
    @Override
    public TransactionDto transfer(Long fromId, Long toId, Long amount, String username) {
        //Checks if transferring to the same account, throws exception if you are
        if(fromId.equals(toId)){
            throw new RuntimeException("Cannot Transfer to the same account");
        }

        //Fetches User, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(()->{
                    return new UserNotFoundException(username);
                });

        //Fetches Account by specified id, throws exception if not found
        Account fromAccount = repoA.findById(fromId)
                .orElseThrow(()-> new AccountNotFoundException(fromId));

        //Fetches Account by specified id, throws exception if not found
        Account toAccount = repoA.findById(toId)
                .orElseThrow(()-> new AccountNotFoundException(toId));

        //Checks ownership of from account
        if(!fromAccount.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Unauthorized Access");
        }


        //Checks if balance is enough for transfer
        if(fromAccount.getBalance()< amount){
            throw new IllegalArgumentException("Insufficient Balance");
        }

        //Performs Debit and Credit
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

        repoA.save(fromAccount);
        repoA.save(toAccount);

        //Create Transaction Record
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setStatus(Status.SUCCESS);
        transaction = repo.save(transaction);

        //Create Ledger Entries
        LedgerEntry debit = new LedgerEntry();
        debit.setAccount(fromAccount);
        debit.setAmount(amount);
        debit.setType(Type.DEBIT);
        repoT.save(debit);

        LedgerEntry credit = new LedgerEntry();
        credit.setAccount(toAccount);
        credit.setAmount(amount);
        credit.setType(Type.CREDIT);
        repoT.save(credit);


        //Create Audit log record
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.TRANSFER);
        auditLog.setDetails("Account "+ fromId + " to " + toId + ":" + fromAccount.getCurrency() + amount);
        auditLog.setPerformedBy(username);
        auditLogRepo.save(auditLog);


        //map Transaction Entity to TransactionDto object using mapper
        return TransactionMapper.toDto(transaction);
    }
}
