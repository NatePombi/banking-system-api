package com.nate.bankingsystemapi.service.transaction;

import com.nate.bankingsystemapi.dto.transaction.FundsRequest;
import com.nate.bankingsystemapi.dto.account.LockedAccounts;
import com.nate.bankingsystemapi.dto.transaction.TransactionDto;
import com.nate.bankingsystemapi.dto.transaction.TransferRequest;
import com.nate.bankingsystemapi.exception.*;
import com.nate.bankingsystemapi.mapper.TransactionMapper;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.TransactionRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.service.account.AccountService;
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

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final LedgerService ledgerService;
    private final AuditService auditService;
    private final TransactionFailureService transactionFailureService;
    private final TransactionCreationService transactionCreationService;


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public TransactionDto transfer(TransferRequest request, String authenticatedUserUsername, Long authenticatedUserId) {

        Transactions transactions = transactionCreationService.createTransaction(request.getRequestID());


        try{
            transactions.markProcessing();

            LockedAccounts accounts = accountService.transfer(request.getFromAccount(), request.getToAccount(),authenticatedUserId,request.getAmount());


           // accountRepository.saveAll(List.of(accounts.fromAccount(),accounts.toAccount()));

            ledgerService.recordDebit(transactions,accounts.fromAccount(),request.getAmount());
            ledgerService.recordCredit(transactions,accounts.toAccount(),request.getAmount());

            transactions.markSuccess();
            transactionRepository.save(transactions);

            auditService.logTransfer(transactions.getId(),authenticatedUserUsername,accounts.fromAccount(),accounts.toAccount(), request.getAmount());

            return TransactionMapper.toDto(transactions,accounts.fromAccount(),accounts.toAccount(),request.getAmount());

        }

        catch (Exception e){
            transactionFailureService.failTransaction(transactions.getId(),e.getMessage());
            throw e;
        }

    }


    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String depositFunds(FundsRequest req, String authenticatedUserUsername, Long authenticatedUserId) {

        //Create transaction (Idempotency gate)
        Transactions transactions = transactionCreationService.createTransaction(req.getRequestID());
        try {
            transactions.markProcessing();

            //Fetching Account by id, throws exception if not found
            Account acc = accountService.deposit(req.getAccountNum(),authenticatedUserId,req.getAmount());

            //save to Ledger
            ledgerService.recordCredit(transactions,acc,req.getAmount());

            transactions.markSuccess();
            transactionRepository.save(transactions);

            auditService.logDeposit(transactions.getId(),authenticatedUserUsername,acc,req.getAmount());

            //Returns Success message
            return "Successfully Deposited " + req.getAmount() + " " + acc.getCurrency();
        }

        catch (Exception e){
            transactionFailureService.failTransaction(transactions.getId(),e.getMessage());
            throw e;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Override
    public String withdrawFunds(FundsRequest req, String authenticatedUserUsername, Long authenticatedUserId) {

        // Creating transaction (Idempotency gate)
        Transactions transactions = transactionCreationService.createTransaction(req.getRequestID());

        try {
            transactions.markProcessing();

            //Fetching Account by id, throws exception if not found
            Account acc = accountService.withdraw(req.getAccountNum(),authenticatedUserId,req.getAmount());


            //save to Ledger
            ledgerService.recordDebit(transactions,acc,req.getAmount());


            auditService.logWithdraw(transactions.getId(),authenticatedUserUsername,acc,req.getAmount());

            transactions.markSuccess();
            transactionRepository.save(transactions);
            //Returns Success message
            return "Successfully withdrew Funds " + req.getAmount() + " " + acc.getCurrency();
        }

        catch (Exception e){
            transactionFailureService.failTransaction(transactions.getId(),e.getMessage());
            throw e;
        }
    }


}
