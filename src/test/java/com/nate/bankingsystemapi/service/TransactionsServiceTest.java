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
    @InjectMocks
    private TransactionService service;


    private Account testAccount;
    private Account testAccount2;
    private User testUser;
    private String reqID;

    @BeforeEach
    void startUp(){
        testUser = new TestUser(11L,"Tester","test","hash-pass");
        testAccount = new TestAccount(1L,testUser.getId());
        testAccount.changeBalance(10000L);
        testAccount2 = new TestAccount(2L,testUser.getId());

        reqID = UUID.randomUUID().toString();

    }

    @Nested
    class Transfer {
        @Test
        void testTransfer_Success() {
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            when(repoT.save(any(Transactions.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),5000L,reqID);

            TransactionDto dto = service.transfer(transfer,11L);

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
                service.transfer(transfer,11L);
            });
        }

        @Test
        void testTransfer_FailUserNotFound(){
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),5000L,reqID);

            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.transfer(transfer,1L);
            });

            assertTrue(ex.getMessage().contains("1"));
        }

        @Test
        void testTransfer_FailFromAccountNotFound(){
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));
            TransferRequest transfer = new TransferRequest(1234567890L,testAccount2.getAccountNum(),5000L,reqID);


            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(transfer,11L);
            });

            assertTrue(ex.getMessage().contains("1234567890"));
        }

        @Test
        void testTransfer_FailToAccountNotFound(){
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),987654321L,5000L,reqID);


            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(transfer,11L);
            });

            assertTrue(ex.getMessage().contains("987654321"));
        }

        @Test
        void testTransfer_FailUnauthorizedAccess(){
            User notOwner = new User("Mark","mark","hashed");
            when(repoU.findById(12L)).thenReturn(Optional.of(notOwner));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),5000L,reqID);


            assertThrows(AccessDeniedException.class,()->{
                service.transfer(transfer,12L);
            });
        }

        @Test
        void testTransfer_FailInsufficientFunds(){
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));
            TransferRequest transfer = new TransferRequest(testAccount.getAccountNum(),testAccount2.getAccountNum(),500000L,reqID);

            assertThrows(IllegalArgumentException.class,()->{
                service.transfer(transfer,11L);
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
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            service.depositFunds(req,11L);

            assertEquals(amount + prevBalance, testAccount.getBalance(),"current balance should be the sum of previous balance and amount deposited");
        }

        @Test
        void testDepositFunds_FailUserNotFound(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),3000L,reqID);
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.depositFunds(req,11L);
            });

            assertTrue(ex.getMessage().contains("11"));
        }

        @Test
        void testDepositFunds_FailAccountNotFound(){
            FundsRequest req = new FundsRequest(3654897L,3000L,reqID);
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));

            assertThrows(AccountNotFoundException.class,()->{
                service.depositFunds(req,11L);
            });

        }

        @Test
        void testDepositFund_FailAccessDenied(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),3000L,reqID);
            when(repoU.findById(13L)).thenReturn(Optional.of(new User()));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            assertThrows(AccessDeniedException.class,()->{
                service.depositFunds(req,13L);
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
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            service.withdrawFunds(req,11L);

            assertEquals(prevBalance - amount,testAccount.getBalance(),"the current balance should be the difference between the previous balance and the amount");
        }


        @Test
        void testWithdrawFunds_FailInsufficientFunds(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),20000L,reqID);

            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            assertThrows(IllegalArgumentException.class,()->{
                service.withdrawFunds(req,11L);
            });
        }


        @Test
        void testWithdrawFunds_FailUserNotFound(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),2000L,reqID);

            Exception ex = assertThrows(UserNotFoundException.class, ()->{
                service.withdrawFunds(req,15L);
            });

            assertTrue(ex.getMessage().contains("15"));
        }


        @Test
        void testWithdrawFunds_FailAccountNotFound(){
            FundsRequest req = new FundsRequest(987654321L,2000L,reqID);
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));

            Exception ex = assertThrows(AccountNotFoundException.class, ()->{
                service.withdrawFunds(req,11L);
            });

        }


        @Test
        void testWithdrawFund_FailAccessDenied(){
            FundsRequest req = new FundsRequest(testAccount.getAccountNum(),3000L,reqID);
            when(repoU.findById(13L)).thenReturn(Optional.of(new User()));
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            assertThrows(AccessDeniedException.class,()->{
                service.withdrawFunds(req,13L);
            });
        }
    }

}
