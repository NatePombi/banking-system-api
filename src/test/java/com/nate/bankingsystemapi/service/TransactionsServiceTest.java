package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
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

    @BeforeEach
    void startUp(){
        testUser = new User(2L,"Tester","test","hash-pass", Role.USER);
        testAccount = new Account(1L,10000L,"ZAR",testUser);
        testAccount2 = new Account(2L,0L,"ZAR",testUser);

    }

    @Nested
    class Transfer {
        @Test
        void testTransfer_Success() {
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            when(repoA.findByIdForUpdate(2L)).thenReturn(Optional.of(testAccount2));
            when(repoT.save(any(Transactions.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TransactionDto dto = service.transfer(1L, 2L, 5000L, "test","UUID1");

            assertNotNull(dto);
            assertEquals(5000L, testAccount.getBalance(), "should decreased by the amount transferred");
            assertEquals(5000L, testAccount2.getBalance(), "should be increased by amount transferred");

            verify(repoT, atLeast(1)).save(any(Transactions.class));
            verify(repoAL).save(any(AuditLog.class));
            verify(repoL, times(2)).save(any(LedgerEntry.class));
        }

        @Test
        void testTransfer_FailSendingToSameAccount(){
            assertThrows(RuntimeException.class,()->{
                service.transfer(1L,1L,5000L,"test","UUID1");
            });
        }

        @Test
        void testTransfer_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.transfer(1L,2L,500L,"test","UUID1");
            });

            assertTrue(ex.getMessage().contains("test"));
        }

        @Test
        void testTransfer_FailFromAccountNotFound(){
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));

            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(1L,2L,400L,"test","UUID1");
            });

            assertTrue(ex.getMessage().contains("1"));
        }

        @Test
        void testTransfer_FailToAccountNotFound(){
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));

            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(1L,2L,400L,"test","UUID1");
            });

            assertTrue(ex.getMessage().contains("2"));
        }

        @Test
        void testTransfer_FailUnauthorizedAccess(){
            User notOwner = new User(33L,"Mark","mark","hashed",Role.USER);
            when(repoU.findByUsername("mark")).thenReturn(Optional.of(notOwner));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            when(repoA.findByIdForUpdate(2L)).thenReturn(Optional.of(testAccount2));

            assertThrows(AccessDeniedException.class,()->{
                service.transfer(1L,2L,400L,"mark","UUID1");
            });
        }

        @Test
        void testTransfer_FailInsufficientFunds(){
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            when(repoA.findByIdForUpdate(2L)).thenReturn(Optional.of(testAccount2));

            assertThrows(IllegalArgumentException.class,()->{
                service.transfer(1L,2L,40000L,"test","UUID1");
            });
        }
    }

    @Nested
    class Deposit{

        @Test
        void testDepositFunds_Success(){
            Long amount = 4000L;
            Long prevBalance = testAccount.getBalance();
            FundsRequest req = new FundsRequest(1L,amount,"UUID1");
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));

            service.depositFunds(req,"test");

            assertEquals(amount + prevBalance, testAccount.getBalance(),"current balance should be the sum of previous balance and amount deposited");
        }

        @Test
        void testDepositFunds_FailUserNotFound(){
            FundsRequest req = new FundsRequest(1L,3000L,"UUID1");
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.depositFunds(req,"test");
            });

            assertTrue(ex.getMessage().contains("test"));
        }

        @Test
        void testDepositFunds_FailAccountNotFound(){
            FundsRequest req = new FundsRequest(1L,3000L,"UUID1");
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));

            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.depositFunds(req,"test");
            });

            assertTrue(ex.getMessage().contains("1"));
        }

        @Test
        void testDepositFund_FailAccessDenied(){
            User user = new User();
            user.setId(22L);
            user.setUsername("mark");
            FundsRequest req = new FundsRequest(1L,3000L,"UUID1");
            when(repoU.findByUsername("mark")).thenReturn(Optional.of(user));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            assertThrows(AccessDeniedException.class,()->{
                service.depositFunds(req,"mark");
            });
        }
    }


    @Nested
    class Withdraw{

        @Test
        void testWithdrawFunds_Success(){
            Long amount = 200L;
            Long prevBalance = testAccount.getBalance();
            FundsRequest req = new FundsRequest(1L,amount,"UUID1");
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));

            service.withdrawFunds(req,"test");

            assertEquals(prevBalance - amount,testAccount.getBalance(),"the current balance should be the difference between the previous balance and the amount");
        }


        @Test
        void testWithdrawFunds_FailInsufficientFunds(){
            FundsRequest req = new FundsRequest(1L,20000L,"UUID1");

            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));

            assertThrows(IllegalArgumentException.class,()->{
                service.withdrawFunds(req,"test");
            });
        }


        @Test
        void testWithdrawFunds_FailUserNotFound(){
            FundsRequest req = new FundsRequest(1L,2000L,"UUID1");

            Exception ex = assertThrows(UserNotFoundException.class, ()->{
                service.withdrawFunds(req,"test");
            });

            assertTrue(ex.getMessage().contains("test"));
        }


        @Test
        void testWithdrawFunds_FailAccountNotFound(){
            FundsRequest req = new FundsRequest(1L,2000L,"UUID1");
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));

            Exception ex = assertThrows(AccountNotFoundException.class, ()->{
                service.withdrawFunds(req,"test");
            });

            assertTrue(ex.getMessage().contains("1"));
        }


        @Test
        void testWithdrawFund_FailAccessDenied(){
            User user = new User();
            user.setId(22L);
            user.setUsername("mark");
            FundsRequest req = new FundsRequest(1L,3000L,"UUID1");
            when(repoU.findByUsername("mark")).thenReturn(Optional.of(user));
            when(repoA.findByIdForUpdate(1L)).thenReturn(Optional.of(testAccount));
            assertThrows(AccessDeniedException.class,()->{
                service.withdrawFunds(req,"mark");
            });
        }
    }

}
