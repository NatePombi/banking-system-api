package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.model.Account;
import com.nate.bankingsystemapi.model.Role;
import com.nate.bankingsystemapi.model.User;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {

    @Mock
    private AccountRepository repoA;
    @Mock
    private UserRepository repoU;
    @InjectMocks
    private AccountService service;

    private PostAccountDto testPost;
    private User testUser;
    private Account testAccount;

    @BeforeEach
    void startUp(){
        testUser = new User(1L,"Tester","test","hash-pass", Role.USER);
        testAccount = new Account(2L,40000L,"ZAR", testUser,0);
        testPost = new PostAccountDto(40000L,"ZAR");

    }


    @DisplayName("Testing Create Account: All possible results")
    @Nested
    class CreateAccount {
        @Test
        void testCreateAccount_Success() {
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.save(any(Account.class))).thenReturn(testAccount);

            AccountDto dto = service.createAccount(testPost, "test");

            assertEquals(testPost.getBalance(), dto.getBalance(), "balance should the same");
            assertEquals(testPost.getCurrency(), dto.getCurrency(), "currency should be the same");

        }

        @Test
        void testCreateAccount_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.createAccount(testPost,"mark");
            });

            assertTrue(ex.getMessage().contains("mark"));
        }

    }

    @DisplayName("Testing get account by id: All possible results")
    @Nested
    class GetAccountById{
        @Test
        void testGetAccountById_Success(){
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountById(2L,"test");

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUser().getId(),dto.getUser(),"should have the same user id");
        }

        @Test
        void testGetAccountById_SuccessEvenIfNotOwner_Admin(){
            User admin = new User(22L,"Admin","admin","admin123",Role.ADMIN);
            when(repoU.findByUsername("admin")).thenReturn(Optional.of(admin));
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountById(2L,"admin");

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUser().getId(),dto.getUser(),"should have the same user id");
        }

        @Test
        void testGetAccountById_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.getAccountById(2L,"test");
            });

            assertTrue(ex.getMessage().contains("test"));
        }

        @Test
        void testGetAccountById_FailAccountNotFound(){
            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.getAccountById(2L,"test");
            });

            assertTrue(ex.getMessage().contains("2"));
        }

        @Test
        void testGetAccountById_FailUnauthorized(){
            User testUser2 = new User(11L,"Mark","mark","mark123",Role.USER);
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));
            when(repoU.findByUsername("mark")).thenReturn(Optional.of(testUser2));

            assertThrows(AccessDeniedException.class,()->{
                service.getAccountById(2L,"mark");
            });

        }

    }

    @DisplayName("Testing Get All User Account: All possible results")
    @Nested
    class GetAllUserAccounts{
        @Test
        void testGetAllUserAccounts_Success(){
            Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
            Page<Account> page = new PageImpl<>(List.of(testAccount));

            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findByUserUsername("test",pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount("test",0,5,"id","desc");

            assertNotNull(dto);
            assertEquals(1,dto.getContent().size(),"should contain one Account");
        }


        @Test
        void testGetAllUserAccounts_SuccessAdminFetchesAll(){
            User admin = new User(22L,"Admin","admin","admin123",Role.ADMIN);
            Account acc1 = new Account();
            Account acc2 = new Account();
            Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
            Page<Account> page = new PageImpl<>(List.of(testAccount,acc1,acc2));

            when(repoU.findByUsername("admin")).thenReturn(Optional.of(admin));
            when(repoA.findAll(pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount("admin",0,5,"id","desc");

            assertNotNull(dto);
            assertEquals(3,dto.getContent().size(),"should contain one Account");
        }

        @Test
        void testGetAllUserAccounts_FailUserNotFound(){

            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.getAllUserAccount("test",0,5,"id","desc");
            });

            assertTrue(ex.getMessage().contains("test"));
        }

        @Test
        void testGetAllUserAccounts_NoAccountsFound(){

            Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
            Page<Account> page = new PageImpl<>(List.of());

            when(repoU.findByUsername("test")).thenReturn(Optional.of(testUser));
            when(repoA.findByUserUsername("test",pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount("test",0,5,"id","desc");

            assertTrue(dto.isEmpty());
        }



    }

}
