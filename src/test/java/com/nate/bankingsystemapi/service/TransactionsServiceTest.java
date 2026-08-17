package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.account.LockedAccounts;
import com.nate.bankingsystemapi.dto.transaction.FundsRequest;
import com.nate.bankingsystemapi.dto.transaction.TransactionDto;
import com.nate.bankingsystemapi.dto.transaction.TransferRequest;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.DuplicateRequestException;
import com.nate.bankingsystemapi.exception.InsufficientAmountException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.account.enums.CurrencyCode;
import com.nate.bankingsystemapi.model.transaction.entity.TestTransaction;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.user.entity.TestUser;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.repository.*;
import com.nate.bankingsystemapi.service.account.AccountService;
import com.nate.bankingsystemapi.service.audit.AuditService;
import com.nate.bankingsystemapi.service.ledger.LedgerService;
import com.nate.bankingsystemapi.service.transaction.TransactionCreationService;
import com.nate.bankingsystemapi.service.transaction.TransactionFailureService;
import com.nate.bankingsystemapi.service.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionsServiceTest {
    @Mock
    private TransactionRepository repoT;
    @Mock
    private AuditLogRepository repoAL;
    @Mock
    private LedgerEntryRepository repoL;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private AuditService auditService;
    @Mock
    private TransactionFailureService transactionFailureService;
    @Mock
    private TransactionCreationService transactionCreationService;
    @Mock
    private AccountService accountService;
    @InjectMocks
    private TransactionService service;


    private Account testAccount;
    private Account testAccount2;
    private User testUser;
    private Transactions testTransaction;
    private String reqID;

    @BeforeEach
    void startUp(){
        testUser = new TestUser(11L,"Tester","test","hash-pass");
        testAccount = Account.create(testUser, CurrencyCode.ZAR);
        testAccount.credit(BigDecimal.valueOf(10000));
        testAccount2 = Account.create(testUser,CurrencyCode.ZAR);
        reqID = UUID.randomUUID().toString();

        testTransaction = new TestTransaction(12L,reqID);


    }

    @Nested
    class Transfer {
        @Test
        void testTransfer_Success() {
            when(transactionCreationService.createTransaction(reqID)).thenReturn(testTransaction);
            when(accountService.transfer(testAccount.getAccountNum(),testAccount2.getAccountNum(),testUser.getId(),BigDecimal.valueOf(5000))).thenReturn(new LockedAccounts(testAccount,testAccount2));

            TransferRequest transferRequest = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            TransactionDto dto = service.transfer(transferRequest,testUser.getUsername(),testUser.getId());

            assertNotNull(dto);

            verify(transactionCreationService).createTransaction(reqID);

            verify(accountService).transfer(testAccount.getAccountNum(),testAccount2.getAccountNum(),testUser.getId(),BigDecimal.valueOf(5000));

        }

        @Test
        void shouldFailTransferDuplicateRequest() {
            when(transactionCreationService.createTransaction(reqID)).thenThrow(new DuplicateRequestException("Request already processed"));
            TransferRequest transferRequest = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            assertThrows(DuplicateRequestException.class,()->{
                service.transfer(transferRequest,"test",testUser.getId());
            });
        }

    }

    @Nested
    class Deposit{

        @Test
        void testDepositFunds_Success(){
            BigDecimal amount = BigDecimal.valueOf(4000);
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),amount,reqID);
            when(transactionCreationService.createTransaction(reqID)).thenReturn(testTransaction);
            when(accountService.deposit(testAccount.getAccountNum(),testUser.getId(),amount)).thenReturn(testAccount);

            String response = service.depositFunds(req,"test",testUser.getId());

            assertNotNull(response);

            verify(transactionCreationService).createTransaction(reqID);
            verify(accountService).deposit(testAccount.getAccountNum(),testUser.getId(),amount);

        }

        @Test
        void shouldFailWithdrawDuplicateRequest() {
            when(transactionCreationService.createTransaction(reqID)).thenThrow(new DuplicateRequestException("Request already processed"));
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            assertThrows(DuplicateRequestException.class,()->{
                service.depositFunds(req,"test",testUser.getId());
            });
        }

    }


    @Nested
    class Withdraw{

        @Test
        void testWithdrawFunds_Success(){
            BigDecimal amount = BigDecimal.valueOf(200);
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),amount,reqID);
            when(transactionCreationService.createTransaction(reqID)).thenReturn(testTransaction);
            when(accountService.withdraw(testAccount.getAccountNum(),testUser.getId(),amount)).thenReturn(testAccount);


            String response = service.withdrawFunds(req,"test",testUser.getId());

            assertNotNull(response);

            verify(transactionCreationService).createTransaction(reqID);
            verify(accountService).withdraw(testAccount.getAccountNum(),testUser.getId(),amount);
        }




        @Test
        void shouldFailWithdrawDuplicateRequest() {
            when(transactionCreationService.createTransaction(reqID)).thenThrow(new DuplicateRequestException("Request already processed"));
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            assertThrows(DuplicateRequestException.class,()->{
                service.withdrawFunds(req,"test",testUser.getId());
            });
        }

    }

}
