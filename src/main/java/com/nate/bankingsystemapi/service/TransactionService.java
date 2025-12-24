package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.mapper.TransactionMapper;
import com.nate.bankingsystemapi.model.*;
import com.nate.bankingsystemapi.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository repo;
    private final AccountRepository repoA;
    private final UserRepository repoU;
    private final LedgerEntryRepository repoT;
    private final AuditLogRepository auditLogRepo;


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public TransactionDto transfer(Long fromId, Long toId, Long amount, String username,String reqId) {

        //Fetches User, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        //Create Transaction Record
        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setStatus(Status.PENDING);
        transaction.setRequestID(reqId);
        transaction.setInstant(Instant.now());
        transaction.setAction(Action.TRANSFER);
        transaction.setUsername(username);

        try{
            repo.save(transaction);
        }
        catch (DataIntegrityViolationException e){
            Optional<Transaction> existing = repo.findByRequestIDAndUsername(reqId, username);
            return TransactionMapper.toDto(existing.get());
        }

        //Checks if transferring to the same account, throws exception if you are
        if (fromId == null || toId == null) {
            transaction.setStatus(Status.FAILED);
            repo.save(transaction);
            throw new IllegalArgumentException("Account ids must be provided");
        }

        if(fromId.equals(toId)){
            transaction.setStatus(Status.FAILED);
            repo.save(transaction);
            throw new IllegalArgumentException("Cannot Transfer to the same account");
        }

        if (amount == null || amount <= 0) {
            transaction.setStatus(Status.FAILED);
            repo.save(transaction);
            throw new IllegalArgumentException("Amount must be > 0");
        }


        //determines the lock order to prevent deadlocks
        Long firstLockId = Math.min(fromId,toId);
        Long secondLockId = Math.max(fromId,toId);


        //Locks rows in the same order for every transfer
        Account first = repoA.findByIdForUpdate(firstLockId)
                .orElseThrow(()-> new AccountNotFoundException(firstLockId));
        Account second = repoA.findByIdForUpdate(secondLockId)
                .orElseThrow(()-> new AccountNotFoundException(secondLockId));

        //Maps locked account back to from/to
        Account fromAccount = (firstLockId.equals(fromId)) ? first : second;
        Account toAccount = (firstLockId.equals(toId)) ? first : second;

        //Checks ownership of from account
        if(!fromAccount.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Unauthorized Access");
        }


        //Checks if balance is enough for transfer
        if(fromAccount.getBalance()< amount){
            transaction.setStatus(Status.FAILED);
            repo.save(transaction);
            throw new IllegalArgumentException("Insufficient Balance");
        }

        //Performs Debit and Credit
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

       transaction.setFromAccount(fromAccount);
       transaction.setToAccount(toAccount);
       transaction.setStatus(Status.SUCCESS);
        transaction = repo.save(transaction);

        //Create Ledger Entries
       recordDebit(fromAccount,amount,transaction,Action.TRANSFER);
        recordCredit(toAccount,amount,transaction,Action.TRANSFER);


        //Create Audit log record
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.TRANSFER);
        auditLog.setDetails("Account "+ fromId + " to " + toId + ":" + fromAccount.getCurrency() + amount);
        auditLog.setPerformedBy(username);
        auditLogRepo.save(auditLog);


        //map Transaction Entity to TransactionDto object using mapper
        return TransactionMapper.toDto(transaction);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String depositFunds(FundsRequest req, String username) {

        //fetching user by username, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(()-> new UserNotFoundException(username));

        //Create transaction (Idempotency gate)
        Transaction transaction = new Transaction();
        transaction.setAction(Action.DEPOSIT);
        transaction.setInstant(Instant.now());
        transaction.setAmount(req.getAmount());
        transaction.setStatus(Status.PENDING);
        transaction.setUsername(username);
        transaction.setRequestID(req.getRequestID());

        try{
            repo.saveAndFlush(transaction);
        }
        catch (DataIntegrityViolationException e){
            return "Request already processed";
        }

        //Fetching Account by id, throws exception if not found
        Account acc = repoA.findByIdForUpdate(req.getAccountId())
                .orElseThrow(()-> new AccountNotFoundException(req.getAccountId()));

        //checks if user ownership, throws exception if user not owner
        if(!acc.getUser().getId().equals(user.getId())){
            transaction.setStatus(Status.FAILED);
            repo.save(transaction);
            throw new AccessDeniedException("Unauthorized Access");
        }



        //Performs the deposit
        acc.setBalance(acc.getBalance() + req.getAmount());

        transaction.setToAccount(acc);
        transaction.setStatus(Status.SUCCESS);
        repo.save(transaction);

        //save to Ledger
        recordCredit(acc, req.getAmount(),transaction,Action.DEPOSIT);

        //Returns Success message
        return "Successfully Deposited " + req.getAmount() +" " + acc.getCurrency();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String withdrawFunds(FundsRequest req, String username) {

        //fetching user by username, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(()-> new UserNotFoundException(username));

        // Creating transaction (Idempotency gate)
        Transaction transaction = new Transaction();
        transaction.setRequestID(req.getRequestID());
        transaction.setInstant(Instant.now());
        transaction.setStatus(Status.PENDING);
        transaction.setAction(Action.WITHDRAW);
        transaction.setAmount(req.getAmount());
        transaction.setUsername(username);

        try{
            repo.saveAndFlush(transaction);
        }catch (DataIntegrityViolationException e){
            return "Request already processed";
        }

        //Fetching Account by id, throws exception if not found
        Account acc = repoA.findByIdForUpdate(req.getAccountId())
                .orElseThrow(()-> new AccountNotFoundException(req.getAccountId()));

        //checks if user ownership, throws exception if user not owner
        if(!acc.getUser().getId().equals(user.getId())){
            transaction.setStatus(Status.FAILED);
            repo.save(transaction);
            throw new AccessDeniedException("Unauthorized Access");
        }


        //Checks if amount is enough for withdraw, throws exception if amount insufficient
        if(acc.getBalance() < req.getAmount()) {
            transaction.setStatus(Status.FAILED);
            repo.save(transaction);
            throw new IllegalArgumentException("Insufficient balance");
        }

        //Performs the withdrawal
        acc.setBalance(acc.getBalance() - req.getAmount());


        transaction.setStatus(Status.SUCCESS);
        transaction.setFromAccount(acc);
        repo.save(transaction);

        //save to Ledger
        recordDebit(acc, req.getAmount(),transaction,Action.WITHDRAW);

        //Returns Success message
        return "Successfully withdrew Funds " + req.getAmount() +" " + acc.getCurrency();
    }

    private void recordDebit(Account acc, Long amount, Transaction transaction,Action action) {
        LedgerEntry debit = new LedgerEntry();
        debit.setAccount(acc);
        debit.setAmount(amount);
        debit.setType(Type.DEBIT);
        debit.setTransaction(transaction);
        debit.setAction(action);
        debit.setCreateAt(Instant.now());
        repoT.save(debit);
    }

    private void recordCredit(Account acc, Long amount, Transaction transaction,Action action) {
        LedgerEntry debit = new LedgerEntry();
        debit.setAccount(acc);
        debit.setAmount(amount);
        debit.setType(Type.CREDIT);
        debit.setTransaction(transaction);
        debit.setAction(action);
        debit.setCreateAt(Instant.now());
        repoT.save(debit);
    }


}
