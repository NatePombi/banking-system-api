package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.model.*;
import com.nate.bankingsystemapi.repository.*;
import org.hibernate.mapping.Any;
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
public class TransactionServiceTest {
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
        testAccount = new Account(1L,10000L,"ZAR",testUser,0);
        testAccount2 = new Account(2L,0L,"ZAR",testUser,0);

    }

    @Nested
    class Transfer {
        @Test
        void testTransfer_Success() {
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findById(1L)).thenReturn(Optional.of(testAccount));
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount2));
            when(repoT.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

            TransactionDto dto = service.transfer(1L, 2L, 5000L, "test");

            assertNotNull(dto);
            assertEquals(5000L, testAccount.getBalance(), "should decreased by the amount transferred");
            assertEquals(5000L, testAccount2.getBalance(), "should be increased by amount transferred");

            verify(repoA, times(2)).save(any(Account.class));
            verify(repoT).save(any(Transaction.class));
            verify(repoAL).save(any(AuditLog.class));
            verify(repoL, times(2)).save(any(LedgerEntry.class));
        }

        @Test
        void testTransfer_FailSendingToSameAccount(){
            assertThrows(RuntimeException.class,()->{
                service.transfer(1L,1L,5000L,"test");
            });
        }

        @Test
        void testTransfer_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.transfer(1L,2L,500L,"test");
            });

            assertTrue(ex.getMessage().contains("test"));
        }

        @Test
        void testTransfer_FailFromAccountNotFound(){
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));

            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(1L,2L,400L,"test");
            });

            assertTrue(ex.getMessage().contains("1"));
        }

        @Test
        void testTransfer_FailToAccountNotFound(){
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findById(1L)).thenReturn(Optional.of(testAccount));

            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.transfer(1L,2L,400L,"test");
            });

            assertTrue(ex.getMessage().contains("2"));
        }

        @Test
        void testTransfer_FailUnauthorizedAccess(){
            User notOwner = new User(33L,"Mark","mark","hashed",Role.USER);
            when(repoU.findByUsername("mark")).thenReturn(Optional.of(notOwner));
            when(repoA.findById(1L)).thenReturn(Optional.of(testAccount));
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount2));

            assertThrows(AccessDeniedException.class,()->{
                service.transfer(1L,2L,400L,"mark");
            });
        }

        @Test
        void testTransfer_FailInsufficientFunds(){
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findById(1L)).thenReturn(Optional.of(testAccount));
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount2));

            assertThrows(IllegalArgumentException.class,()->{
                service.transfer(1L,2L,40000L,"test");
            });
        }
    }

}
