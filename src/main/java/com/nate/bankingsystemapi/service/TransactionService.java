package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.dto.TransferRequest;
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
    public TransactionDto transfer(TransferRequest request, Long userId) {

        //Fetches User, throws exception if not found
        User user = repoU.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        //Create Transaction Record
        Transactions transactions = new Transactions(request.getAmount(),user.getUsername(), request.getRequestID());
        transactions.changeAction(Action.TRANSFER);
        try{
            repo.save(transactions);
        }
        catch (DataIntegrityViolationException e){
            Optional<Transactions> existing = repo.findByRequestIDAndUsername(request.getRequestID(), user.getUsername());
            return TransactionMapper.toDto(existing.get());
        }

        //Checks if transferring to the same account, throws exception if you are
        if (request.getFromAccount() == null || request.getToAccount() == null) {
            transactions.changeStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Account ids must be provided");
        }

        if(request.getFromAccount().equals(request.getToAccount())){
            transactions.changeStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Cannot Transfer to the same account");
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            transactions.changeStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Amount must be > 0");
        }


        //determines the lock order to prevent deadlocks
        Long firstLockId = Math.min(request.getFromAccount(), request.getToAccount());
        Long secondLockId = Math.max(request.getFromAccount(), request.getToAccount());


        //Locks rows in the same order for every transfer
        Account first = repoA.findByAccountNumForUpdate(firstLockId)
                .orElseThrow(()-> new AccountNotFoundException(firstLockId));
        Account second = repoA.findByAccountNumForUpdate(secondLockId)
                .orElseThrow(()-> new AccountNotFoundException(secondLockId));

        //Maps locked account back to from/to
        Account fromAccount = (firstLockId.equals(request.getFromAccount())) ? first : second;
        Account toAccount = (firstLockId.equals(request.getToAccount())) ? first : second;

        //Checks ownership of from account
        if(!fromAccount.getUserId().equals(user.getId())){
            throw new AccessDeniedException("Unauthorized Access");
        }


        //Checks if balance is enough for transfer
        if(fromAccount.getBalance()< request.getAmount()){
            transactions.changeStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Insufficient Balance");
        }

        //Performs Debit and Credit
        fromAccount.changeBalance(fromAccount.getBalance() - request.getAmount());
        toAccount.changeBalance(toAccount.getBalance() + request.getAmount());

       transactions.changeFromAccount(fromAccount);
       transactions.changeToAccount(toAccount);
       transactions.changeStatus(Status.SUCCESS);
        transactions = repo.save(transactions);

        //Create Ledger Entries
       recordDebit(fromAccount,request.getAmount(), transactions,Action.TRANSFER);
        recordCredit(toAccount, request.getAmount(), transactions,Action.TRANSFER);


        //Create Audit log record
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(Action.TRANSFER);
        auditLog.setDetails("Account "+ fromAccount + " to " + toAccount + ":" + fromAccount.getCurrency() + request.getAmount());
        auditLog.setPerformedBy(user.getUsername());
        auditLogRepo.save(auditLog);


        //map Transaction Entity to TransactionDto object using mapper
        return TransactionMapper.toDto(transactions);
    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String depositFunds(FundsRequest req, Long userId) {

        //fetching user by username, throws exception if not found
        User user = repoU.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        //Create transaction (Idempotency gate)
        Transactions transactions = new Transactions(req.getAmount(),user.getUsername(), req.getRequestID());
        transactions.changeAction(Action.DEPOSIT);

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
        if(!acc.getUserId().equals(user.getId())){
            transactions.changeStatus(Status.FAILED);
            repo.save(transactions);
            throw new AccessDeniedException("Unauthorized Access");
        }



        //Performs the deposit
        acc.changeBalance(acc.getBalance() + req.getAmount());

        transactions.changeToAccount(acc);
        transactions.changeStatus(Status.SUCCESS);
        repo.save(transactions);

        //save to Ledger
        recordCredit(acc, req.getAmount(), transactions,Action.DEPOSIT);

        //Returns Success message
        return "Successfully Deposited " + req.getAmount() +" " + acc.getCurrency();
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String withdrawFunds(FundsRequest req, Long userId) {

        //fetching user by username, throws exception if not found
        User user = repoU.findById(userId)
                .orElseThrow(()-> new UserNotFoundException(userId));

        // Creating transaction (Idempotency gate)
        Transactions transactions = new Transactions(req.getAmount(),user.getUsername(), req.getRequestID());
        transactions.changeAction(Action.WITHDRAW);

        try{
            repo.saveAndFlush(transactions);
        }catch (DataIntegrityViolationException e){
            throw new DuplicateRequestException("Request already processed");
        }

        //Fetching Account by id, throws exception if not found
        Account acc = repoA.findByAccountNumForUpdate(req.getAccountNum())
                .orElseThrow(AccountNotFoundException::new);

        //checks if user ownership, throws exception if user not owner
        if(!acc.getUserId().equals(user.getId())){
            transactions.changeStatus(Status.FAILED);
            repo.save(transactions);
            throw new AccessDeniedException("Unauthorized Access");
        }


        //Checks if amount is enough for withdraw, throws exception if amount insufficient
        if(acc.getBalance() < req.getAmount()) {
            transactions.changeStatus(Status.FAILED);
            repo.save(transactions);
            throw new IllegalArgumentException("Insufficient balance");
        }

        //Performs the withdrawal
        acc.changeBalance(acc.getBalance() - req.getAmount());


        transactions.changeStatus(Status.SUCCESS);
        transactions.changeFromAccount(acc);
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
