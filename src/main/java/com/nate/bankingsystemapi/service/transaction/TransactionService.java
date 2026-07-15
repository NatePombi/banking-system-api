package com.nate.bankingsystemapi.service.transaction;

import com.nate.bankingsystemapi.dto.transaction.FundsRequest;
import com.nate.bankingsystemapi.dto.account.LockedAccounts;
import com.nate.bankingsystemapi.dto.transaction.TransactionDto;
import com.nate.bankingsystemapi.dto.transaction.TransferRequest;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.DuplicateRequestException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.mapper.TransactionMapper;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.repository.*;
import com.nate.bankingsystemapi.service.audit.AuditService;
import com.nate.bankingsystemapi.service.ledger.LedgerService;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@AllArgsConstructor
public class TransactionService implements ITransactionService {

    private final TransactionRepository repo;
    private final AccountRepository repoA;
    private final UserRepository repoU;
    private final TransactionFailureService transactionFailureService;
    private final LedgerService ledgerService;
    private final AuditService auditService;


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public TransactionDto transfer(TransferRequest request, String username) {

        User user = validateUser(username);

        validateAccounts(request);

        Transactions transactions = createTransaction(request.getRequestID());


        try{
            transactions.markProcessing();

            LockedAccounts accounts = lockedAccounts(request);

            Account from = accounts.fromAccount();
            Account to = accounts.toAccount();

            validateOwnership(from,user);

            validateAmount(request.getAmount(),from.getBalance());

            from.debit(request.getAmount());
            to.credit(request.getAmount());

            repoA.saveAll(List.of(from,to));

            ledgerService.recordDebit(transactions,from,request.getAmount());
            ledgerService.recordCredit(transactions,to,request.getAmount());

            transactions.markSuccess();

            auditService.logTransfer(transactions,user,from,to, request.getAmount());

            return TransactionMapper.toDto(transactions,from,to,request.getAmount());

        }

        catch (Exception e){

            transactions.markFailed(e.getMessage());

            repo.save(transactions);

            throw e;
        }

    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String depositFunds(FundsRequest req, String username) {

        //throws exception if User not found
        User user = validateUser(username);

        //Create transaction (Idempotency gate)
        Transactions transactions = createTransaction(req.getRequestID());
        try {
            transactions.markProcessing();

            //Fetching Account by id, throws exception if not found
            Account acc = repoA.findByAccountNumForUpdate(req.getAccountNum())
                    .orElseThrow(AccountNotFoundException::new);

            //checks if user ownership, throws exception if user not owner
            validateOwnership(acc,user);


            //Performs the deposit
            acc.credit(req.getAmount());


            //save to Ledger
            ledgerService.recordCredit(transactions,acc,req.getAmount());

            transactions.markSuccess();

            //Returns Success message
            return "Successfully Deposited " + req.getAmount() + " " + acc.getCurrency();
        }

        catch (Exception e){
            transactions.markFailed(e.getMessage());

            repo.save(transactions);

            throw e;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String withdrawFunds(FundsRequest req, String username) {

        //throws exception if User not found
        User user = validateUser(username);

        // Creating transaction (Idempotency gate)
        Transactions transactions = createTransaction(req.getRequestID());

        try {
            transactions.markProcessing();

            //Fetching Account by id, throws exception if not found
            Account acc = repoA.findByAccountNumForUpdate(req.getAccountNum())
                    .orElseThrow(AccountNotFoundException::new);

            //checks if user ownership, throws exception if user not owner
            validateOwnership(acc,user);


            //Checks if amount is enough for withdraw, throws exception if amount insufficient
            validateAmount(req.getAmount(),acc.getBalance());

            //Performs the withdrawal
            acc.debit(req.getAmount());


            //save to Ledger
            ledgerService.recordDebit(transactions,acc,req.getAmount());

            //Returns Success message
            return "Successfully withdrew Funds " + req.getAmount() + " " + acc.getCurrency();
        }

        catch (Exception e){
            transactions.markFailed(e.getMessage());

            repo.save(transactions);
            throw e;
        }
    }



    private Transactions createTransaction(String key){
        Transactions transactions = Transactions.create(key);

        try {
            return repo.save(transactions);
        }
        catch (DataIntegrityViolationException e){
            throw new DuplicateRequestException("Request already processed");
        }

    }

    private LockedAccounts lockedAccounts(TransferRequest request){

        Long acc1 = Math.min(request.getFromAccount(), request.getToAccount());
        Long acc2 = Math.max(request.getFromAccount(), request.getToAccount());

        Account accA = repoA.findByAccountNumForUpdate(acc1).orElseThrow(AccountNotFoundException::new);
        Account accB = repoA.findByAccountNumForUpdate(acc2).orElseThrow(AccountNotFoundException::new);

        Account from = acc1.equals(request.getFromAccount()) ? accA: accB;
        Account to = acc2.equals(request.getToAccount()) ? accB:accA;

        return new LockedAccounts(from, to);
    }

    private User validateUser(String username){
        User user = repoU.findByUsername(username).orElseThrow(()-> new UserNotFoundException(username));

        if(!repoA.existsByUser(user)){
            throw new AccountNotFoundException(user.getUsername());
        }

        return user;
    }

    private void validateAccounts(TransferRequest request){

        if(request.getFromAccount() == null){
            throw new IllegalArgumentException("From account is required");
        }

        if(request.getToAccount() == null){
            throw new IllegalArgumentException("To account is required");
        }

        if(request.getFromAccount().equals(request.getToAccount())){
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
    }

    private void validateAmount(BigDecimal amount, BigDecimal balance){

        if(amount == null){
            throw new IllegalArgumentException("Amount is required");
        }

        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if (amount.compareTo(balance) > 0){
            throw new IllegalArgumentException("Insufficient balance");
        }
    }

    private void validateOwnership(Account fromAccount,User user){

        if(!fromAccount.getUser().getId().equals(user.getId())){
            throw new AccessDeniedException("Unauthorized Access");
        }

    }





}
