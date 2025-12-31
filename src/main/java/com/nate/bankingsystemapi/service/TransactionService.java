package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.DuplicateRequestException;
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
    public TransactionDto transfer(Long fromAccountNum, Long toAccountNum, Long amount, String username,String reqId) {

        //Fetches User, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        //Create Transaction Record
        Transactions transactions = new Transactions();
        transactions.setAmount(amount);
        transactions.setStatus(Status.PENDING);
        transactions.setRequestID(reqId);
        transactions.setInstant(Instant.now());
        transactions.setAction(Action.TRANSFER);
        transactions.setUsername(username);

        try{
            repo.save(transactions);
        }
        catch (DataIntegrityViolationException e){
            Optional<Transactions> existing = repo.findByRequestIDAndUsername(reqId, username);
            return TransactionMapper.toDto(existing.get());
        }

        //Checks if transferring to the same account, throws exception if you are
        if (fromAccountNum == null || toAccountNum == null) {
            transactions.setStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Account ids must be provided");
        }

        if(fromAccountNum.equals(toAccountNum)){
            transactions.setStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Cannot Transfer to the same account");
        }

        if (amount == null || amount <= 0) {
            transactions.setStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Amount must be > 0");
        }


        //determines the lock order to prevent deadlocks
        Long firstLockId = Math.min(fromAccountNum,toAccountNum);
        Long secondLockId = Math.max(fromAccountNum,toAccountNum);


        //Locks rows in the same order for every transfer
        Account first = repoA.findByAccountNumForUpdate(firstLockId)
                .orElseThrow(()-> new AccountNotFoundException(firstLockId));
        Account second = repoA.findByAccountNumForUpdate(secondLockId)
                .orElseThrow(()-> new AccountNotFoundException(secondLockId));

        //Maps locked account back to from/to
        Account fromAccount = (firstLockId.equals(fromAccountNum)) ? first : second;
        Account toAccount = (firstLockId.equals(toAccountNum)) ? first : second;

        //Checks ownership of from account
        if(!fromAccount.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Unauthorized Access");
        }


        //Checks if balance is enough for transfer
        if(fromAccount.getBalance()< amount){
            transactions.setStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Insufficient Balance");
        }

        //Performs Debit and Credit
        fromAccount.setBalance(fromAccount.getBalance() - amount);
        toAccount.setBalance(toAccount.getBalance() + amount);

       transactions.setFromAccount(fromAccount);
       transactions.setToAccount(toAccount);
       transactions.setStatus(Status.SUCCESS);
        transactions = repo.save(transactions);

        //Create Ledger Entries
       recordDebit(fromAccount,amount, transactions,Action.TRANSFER);
        recordCredit(toAccount,amount, transactions,Action.TRANSFER);


        //Create Audit log record
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.TRANSFER);
        auditLog.setDetails("Account "+ fromAccount + " to " + toAccount + ":" + fromAccount.getCurrency() + amount);
        auditLog.setPerformedBy(username);
        auditLogRepo.save(auditLog);


        //map Transaction Entity to TransactionDto object using mapper
        return TransactionMapper.toDto(transactions);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String depositFunds(FundsRequest req, String username) {

        //fetching user by username, throws exception if not found
        User user = repoU.findByUsername(username)
                .orElseThrow(()-> new UserNotFoundException(username));

        //Create transaction (Idempotency gate)
        Transactions transactions = new Transactions();
        transactions.setAction(Action.DEPOSIT);
        transactions.setInstant(Instant.now());
        transactions.setAmount(req.getAmount());
        transactions.setStatus(Status.PENDING);
        transactions.setUsername(username);
        transactions.setRequestID(req.getRequestID());

        try{
            repo.saveAndFlush(transactions);
        }
        catch (DataIntegrityViolationException e){
            throw new DuplicateRequestException("Request already processed");
        }

        //Fetching Account by id, throws exception if not found
        Account acc = repoA.findByAccountNumForUpdate(req.getAccountNum())
                .orElseThrow(AccountNotFoundException::new);

        //checks if user ownership, throws exception if user not owner
        if(!acc.getUser().getId().equals(user.getId())){
            transactions.setStatus(Status.FAILED);
            repo.save(transactions);
            throw new AccessDeniedException("Unauthorized Access");
        }



        //Performs the deposit
        acc.setBalance(acc.getBalance() + req.getAmount());

        transactions.setToAccount(acc);
        transactions.setStatus(Status.SUCCESS);
        repo.save(transactions);

        //save to Ledger
        recordCredit(acc, req.getAmount(), transactions,Action.DEPOSIT);

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
        Transactions transactions = new Transactions();
        transactions.setRequestID(req.getRequestID());
        transactions.setInstant(Instant.now());
        transactions.setStatus(Status.PENDING);
        transactions.setAction(Action.WITHDRAW);
        transactions.setAmount(req.getAmount());
        transactions.setUsername(username);

        try{
            repo.saveAndFlush(transactions);
        }catch (DataIntegrityViolationException e){
            throw new DuplicateRequestException("Request already processed");
        }

        //Fetching Account by id, throws exception if not found
        Account acc = repoA.findByAccountNumForUpdate(req.getAccountNum())
                .orElseThrow(AccountNotFoundException::new);

        //checks if user ownership, throws exception if user not owner
        if(!acc.getUser().getId().equals(user.getId())){
            transactions.setStatus(Status.FAILED);
            repo.save(transactions);
            throw new AccessDeniedException("Unauthorized Access");
        }


        //Checks if amount is enough for withdraw, throws exception if amount insufficient
        if(acc.getBalance() < req.getAmount()) {
            transactions.setStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Insufficient balance");
        }

        //Performs the withdrawal
        acc.setBalance(acc.getBalance() - req.getAmount());


        transactions.setStatus(Status.SUCCESS);
        transactions.setFromAccount(acc);
        repo.save(transactions);

        //save to Ledger
        recordDebit(acc, req.getAmount(), transactions,Action.WITHDRAW);

        //Returns Success message
        return "Successfully withdrew Funds " + req.getAmount() +" " + acc.getCurrency();
    }

    private void recordDebit(Account acc, Long amount, Transactions transactions, Action action) {
        LedgerEntry debit = new LedgerEntry();
        debit.setAccount(acc);
        debit.setAmount(amount);
        debit.setType(Type.DEBIT);
        debit.setTransactions(transactions);
        debit.setAction(action);
        debit.setCreateAt(Instant.now());
        repoT.save(debit);
    }

    private void recordCredit(Account acc, Long amount, Transactions transactions, Action action) {
        LedgerEntry debit = new LedgerEntry();
        debit.setAccount(acc);
        debit.setAmount(amount);
        debit.setType(Type.CREDIT);
        debit.setTransactions(transactions);
        debit.setAction(action);
        debit.setCreateAt(Instant.now());
        repoT.save(debit);
    }


}
