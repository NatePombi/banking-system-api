package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.dto.TransferRequest;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.model.*;
import com.nate.bankingsystemapi.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

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
    @InjectMocks
    private TransactionService service;


    private Account testAccount;
    private Account testAccount2;
    private User testUser;
    private String reqID;

    @BeforeEach
    void startUp(){
        testUser = new TestUser(11L,"Tester","test","hash-pass");
        testAccount = new TestAccount(1L,testUser,CurrencyCode.ZAR);
        testAccount.changeBalance(10000L);
        testAccount2 = new TestAccount(2L,testUser,CurrencyCode.ZAR);

        reqID = UUID.randomUUID().toString();

    }

    @Nested
    class Transfer {
        @Test
        void testTransfer_Success() {
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.existsByUser(testUser)).thenReturn(true);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            when(repoT.save(any(Transactions.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),5000L,reqID);

            TransactionDto dto = service.transfer(transfer,testUser);

            assertNotNull(dto);
            assertEquals(5000L, testAccount.getBalance(), "should decreased by the amount transferred");
            assertEquals(5000L, testAccount2.getBalance(), "should be increased by amount transferred");

            verify(repoT, atLeast(1)).save(any(Transactions.class));
            verify(repoAL).save(any(AuditLog.class));
            verify(repoL, times(2)).save(any(LedgerEntry.class));
        }

        @Test
        void testTransfer_FailSendingToSameAccount(){
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount.getAccountNum(),5000L,reqID);

            assertThrows(RuntimeException.class,()->{
                service.transfer(transfer,testUser);
            });
        }

        @Test
        void testTransfer_FailUserNotFound(){
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),5000L,reqID);

            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.transfer(transfer,testUser);
            });

            assertTrue(ex.getMessage().contains(testUser.getUsername().toString()));
        }

        @Test
        void testTransfer_FailFromAccountNotFound(){
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            TransferRequest transfer = new TransferRequest(123567890L,testAccount2.getAccountNum(),5000L,reqID);


            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(transfer,testUser);
            });

            assertTrue(ex.getMessage().contains("123567890"));
        }

        @Test
        void testTransfer_FailToAccountNotFound(){
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),987654321L,5000L,reqID);


            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(transfer,testUser);
            });

            assertTrue(ex.getMessage().contains("987654321"));
        }

        @Test
        void testTransfer_FailUnauthorizedAccess(){
            User notOwner = new TestUser(12L,"Mark","mark","hashed");
            when(repoU.existsByIdAndUsername(notOwner.getId(),notOwner.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.existsByUser(notOwner)).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),5000L,reqID);


            assertThrows(AccessDeniedException.class,()->{
                service.transfer(transfer,notOwner);
            });
        }

        @Test
        void testTransfer_FailInsufficientFunds(){
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),500000L,reqID);

            assertThrows(IllegalArgumentException.class,()->{
                service.transfer(transfer,testUser);
            });
        }
    }

    @Nested
    class Deposit{

        @Test
        void testDepositFunds_Success(){
            Long amount = 4000L;
            Long prevBalance = testAccount.getBalance();
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),amount,reqID);
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            service.depositFunds(req,testUser);

            assertEquals(amount + prevBalance, testAccount.getBalance(),"current balance should be the sum of previous balance and amount deposited");
        }

        @Test
        void testDepositFunds_FailUserNotFound(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),3000L,reqID);
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.depositFunds(req,testUser);
            });

            assertTrue(ex.getMessage().contains(testUser.getUsername()));
        }

        @Test
        void testDepositFunds_FailAccountNotFound(){
            FundsRequest req = new FundsRequest(3654897L,3000L,reqID);
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            assertThrows(AccountNotFoundException.class,()->{
                service.depositFunds(req,testUser);
            });

        }

        @Test
        void testDepositFund_FailAccessDenied(){
            User testUser1 = new User();
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),3000L,reqID);
            when(repoU.existsByIdAndUsername(testUser1.getId(),testUser1.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            assertThrows(AccessDeniedException.class,()->{
                service.depositFunds(req,testUser1);
            });
        }
    }


    @Nested
    class Withdraw{

        @Test
        void testWithdrawFunds_Success(){
            Long amount = 200L;
            Long prevBalance = testAccount.getBalance();
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),amount,reqID);
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            service.withdrawFunds(req,testUser);

            assertEquals(prevBalance - amount,testAccount.getBalance(),"the current balance should be the difference between the previous balance and the amount");
        }


        @Test
        void testWithdrawFunds_FailInsufficientFunds(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),20000L,reqID);
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            assertThrows(IllegalArgumentException.class,()->{
                service.withdrawFunds(req,testUser);
            });
        }


        @Test
        void testWithdrawFunds_FailUserNotFound(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),2000L,reqID);

            Exception ex = assertThrows(UserNotFoundException.class, ()->{
                service.withdrawFunds(req,testUser);
            });

            assertTrue(ex.getMessage().contains(testUser.getUsername()));
        }


        @Test
        void testWithdrawFunds_FailAccountNotFound(){
            FundsRequest req = new FundsRequest(987654321L,2000L,reqID);
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);

            assertThrows(AccountNotFoundException.class, ()->{
                service.withdrawFunds(req,testUser);
            });

        }


        @Test
        void testWithdrawFund_FailAccessDenied(){
            User testUser1 = new User();
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),3000L,reqID);
            when(repoU.existsByIdAndUsername(testUser1.getId(),testUser1.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            assertThrows(AccessDeniedException.class,()->{
                service.withdrawFunds(req,testUser1);
            });
        }
    }

}
