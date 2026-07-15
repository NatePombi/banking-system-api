package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.transaction.FundsRequest;
import com.nate.bankingsystemapi.dto.transaction.TransactionDto;
import com.nate.bankingsystemapi.dto.transaction.TransferRequest;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.DuplicateRequestException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.account.enums.CurrencyCode;
import com.nate.bankingsystemapi.model.transaction.entity.TestTransaction;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.user.entity.TestUser;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.repository.*;
import com.nate.bankingsystemapi.service.audit.AuditService;
import com.nate.bankingsystemapi.service.ledger.LedgerService;
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
    private UserRepository repoU;
    @Mock
    private AccountRepository repoA;
    @Mock
    private AuditLogRepository repoAL;
    @Mock
    private LedgerEntryRepository repoL;
    @Mock
    private TransactionFailureService failureService;
    @Mock
    private LedgerService ledgerService;
    @Mock
    private AuditService auditService;
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
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(true);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            when(repoA.saveAll(any())).thenReturn(List.of());


            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            TransactionDto dto = service.transfer(transfer,"test");

            assertNotNull(dto);
            assertEquals(BigDecimal.valueOf(5000), testAccount.getBalance(), "should decreased by the amount transferred");
            assertEquals(BigDecimal.valueOf(5000), testAccount2.getBalance(), "should be increased by amount transferred");

            verify(repoT, atLeast(1)).save(any(Transactions.class));
        }

        @Test
        void shouldFailTransferDuplicateRequest() {
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(true);
            when(repoT.save(any(Transactions.class))).thenThrow(new DuplicateRequestException("Request already processed"));
            TransferRequest transferRequest = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            assertThrows(DuplicateRequestException.class,()->{
                service.transfer(transferRequest,"test");
            });
        }

        @Test
        void testTransfer_FailSendingToSameAccount(){
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            assertThrows(RuntimeException.class,()->{
                service.transfer(transfer,"test");
            });
        }

        @Test
        void testTransfer_FailUserNotFound(){
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.transfer(transfer,"invalid");
            });

            assertTrue(ex.getMessage().contains("invalid"));
        }

        @Test
        void testTransfer_FailFromAccountNotFound(){
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);
            TransferRequest transfer = new TransferRequest(123567890L,testAccount2.getAccountNum(),BigDecimal.valueOf(5000),reqID);


            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(transfer,"test");
            });

        }

        @Test
        void testTransfer_FailToAccountNotFound(){
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),987654321L,BigDecimal.valueOf(5000),reqID);


            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(transfer,"test");
            });

        }

        @Test
        void testTransfer_FailUnauthorizedAccess(){
            User notOwner = new TestUser(12L,"Mark","mark","hashed");
            when(repoU.findByUsername(notOwner.getUsername())).thenReturn(Optional.of(notOwner));
            when(repoA.existsByUser(notOwner)).thenReturn(Boolean.TRUE);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),BigDecimal.valueOf(5000),reqID);


            assertThrows(AccessDeniedException.class,()->{
                service.transfer(transfer,notOwner.getUsername());
            });
        }

        @Test
        void testTransfer_FailInsufficientFunds(){
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);

            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),BigDecimal.valueOf(500000),reqID);

            assertThrows(IllegalArgumentException.class,()->{
                service.transfer(transfer,"test");
            });
        }
    }

    @Nested
    class Deposit{

        @Test
        void testDepositFunds_Success(){
            BigDecimal amount = BigDecimal.valueOf(4000);
            BigDecimal prevBalance = testAccount.getBalance();
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),amount,reqID);
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            service.depositFunds(req,"test");

            assertEquals(amount.add(prevBalance), testAccount.getBalance(),"current balance should be the sum of previous balance and amount deposited");
        }

        @Test
        void testDepositFunds_FailUserNotFound(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),BigDecimal.valueOf(3000),reqID);
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.depositFunds(req,"invalid");
            });

            assertTrue(ex.getMessage().contains("invalid"));
        }

        @Test
        void testDepositFunds_FailAccountNotFound(){
            FundsRequest req = new FundsRequest(3654897L,BigDecimal.valueOf(3000),reqID);
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            assertThrows(AccountNotFoundException.class,()->{
                service.depositFunds(req,"test");
            });

        }

        @Test
        void testDepositFund_FailAccessDenied(){
            User testUser1 = User.createUser("Mark","marky","hashed-password");
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),BigDecimal.valueOf(3000),reqID);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);
            when(repoU.findByUsername(testUser1.getUsername())).thenReturn(Optional.of(testUser1));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.existsByUser(testUser1)).thenReturn(Boolean.TRUE);

            assertThrows(AccessDeniedException.class,()->{
                service.depositFunds(req,testUser1.getUsername());
            });
        }
    }


    @Nested
    class Withdraw{

        @Test
        void testWithdrawFunds_Success(){
            BigDecimal amount = BigDecimal.valueOf(200);
            BigDecimal prevBalance = testAccount.getBalance();
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),amount,reqID);
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);

            service.withdrawFunds(req,"test");

            assertEquals(prevBalance.subtract(amount),testAccount.getBalance(),"the current balance should be the difference between the previous balance and the amount");
        }


        @Test
        void testWithdrawFunds_FailInsufficientFunds(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),BigDecimal.valueOf(20000),reqID);
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            assertThrows(IllegalArgumentException.class,()->{
                service.withdrawFunds(req,"test");
            });
        }


        @Test
        void testWithdrawFunds_FailUserNotFound(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),BigDecimal.valueOf(2000),reqID);

            Exception ex = assertThrows(UserNotFoundException.class, ()->{
                service.withdrawFunds(req,"invalid");
            });

            assertTrue(ex.getMessage().contains("invalid"));
        }


        @Test
        void testWithdrawFunds_FailAccountNotFound(){
            FundsRequest req = new FundsRequest(987654321L,BigDecimal.valueOf(2000),reqID);
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);


            assertThrows(AccountNotFoundException.class, ()->{
                service.withdrawFunds(req,"test");
            });

        }


        @Test
        void testWithdrawFund_FailAccessDenied(){
            User testUser1 = User.createUser("Mark","marky","hashed-password");
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),BigDecimal.valueOf(3000),reqID);
            when(repoT.save(any(Transactions.class))).thenReturn(testTransaction);
            when(repoU.findByUsername(testUser1.getUsername())).thenReturn(Optional.of(testUser1));
            when(repoA.existsByUser(testUser1)).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            assertThrows(AccessDeniedException.class,()->{
                service.withdrawFunds(req,testUser1.getUsername());
            });
        }
    }

}
