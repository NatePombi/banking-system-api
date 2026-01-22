package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.model.*;
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

import java.security.SecureRandom;
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
        testUser = new TestUser(1L,"Tester","test","hash-pass");
        testAccount = new TestAccount(2L,testUser.getId());
        testAccount.changeBalance(40000L);
        testPost = new PostAccountDto(40000L);

    }


    @DisplayName("Testing Create Account: All possible results")
    @Nested
    class CreateAccount {
        @Test
        void testCreateAccount_Success() {
            when(repoU.existsById(testUser.getId())).thenReturn(true);
            when(repoA.save(any(Account.class))).thenReturn(testAccount);

            AccountDto dto = service.createAccount(testPost, testUser.getId());

            assertEquals(testPost.getBalance(), dto.getBalance(), "balance should the same");

        }

        @Test
        void testCreateAccount_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.createAccount(testPost,testUser.getId());
            });

            assertTrue(ex.getMessage().contains(testUser.getId().toString()));
        }

    }

    @DisplayName("Testing get account by id: All possible results")
    @Nested
    class GetAccountById{
        @Test
        void testGetAccountById_Success(){
            when(repoU.findById(testUser.getId())).thenReturn(Optional.of(testUser));
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountById(2L,testUser.getId());

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUserId(),dto.getUser(),"should have the same user id");
        }

        @Test
        void testGetAccountById_SuccessEvenIfNotOwner_Admin(){
            User admin = new User("Admin","admin","admin123");
            admin.changeRole(Role.ADMIN);
            when(repoU.findById(22L)).thenReturn(Optional.of(admin));
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountById(2L,22L);

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUserId(),dto.getUser(),"should have the same user id");
        }

        @Test
        void testGetAccountById_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.getAccountById(2L,12L);
            });

            assertTrue(ex.getMessage().contains("12"));
        }

        @Test
        void testGetAccountById_FailAccountNotFound(){
            when(repoU.findById(12L)).thenReturn(Optional.of(testUser));
            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.getAccountById(2L,12L);
            });

            assertTrue(ex.getMessage().contains("2"));
        }

        @Test
        void testGetAccountById_FailUnauthorized(){
            User testUser2 = new User("Mark","mark","mark123");
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));
            when(repoU.findById(13L)).thenReturn(Optional.of(testUser2));

            assertThrows(AccessDeniedException.class,()->{
                service.getAccountById(2L,13L);
            });

        }

    }

    @DisplayName("Testing get account by id: All possible results")
    @Nested
    class GetAccountByAccountNum{
        @Test
        void testGetAccountByAccountNum_Success(){
            when(repoU.findById(2L)).thenReturn(Optional.of(testUser));
            when(repoA.findByAccountNum(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountByAccountNumber(testAccount.getAccountNum(),2L);

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUserId(),dto.getUser(),"should have the same user ID");
        }

        @Test
        void testGetAccountByAccountNum_SuccessEvenIfNotOwner_Admin(){
            User admin = new TestUser(22L,"Admin","admin","admin123");
            admin.changeRole(Role.ADMIN);
            when(repoU.findById(22L)).thenReturn(Optional.of(admin));
            when(repoA.findByAccountNum(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountByAccountNumber(testAccount.getAccountNum(),22L);

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(1L,dto.getUser(),"should have the same user id");
        }

        @Test
        void testGetAccountByAccountNum_FailUserNotFound(){
            assertThrows(UserNotFoundException.class,()->{
                service.getAccountByAccountNumber(3654987L,1L);
            });
        }

        @Test
        void testGetAccountByAccountNum_FailAccountNotFound(){
            when(repoU.findById(2L)).thenReturn(Optional.of(testUser));
            assertThrows(AccountNotFoundException.class,()->{
                service.getAccountByAccountNumber(3654987L,2L);
            });
        }

        @Test
        void testGetAccountByAccountNum_FailUnauthorized(){
            User testUser2 = new User("Mark","mark","mark123");
            when(repoA.findByAccountNum(3654987L)).thenReturn(Optional.of(testAccount));
            when(repoU.findById(11L)).thenReturn(Optional.of(testUser2));

            assertThrows(AccessDeniedException.class,()->{
                service.getAccountByAccountNumber(3654987L,11L);
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

            when(repoU.findById(2L)).thenReturn(Optional.of(testUser));
            when(repoA.findByUserId(2L,pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount(2L,0,5,"id","desc");

            assertNotNull(dto);
            assertEquals(1,dto.getContent().size(),"should contain one Account");
        }


        @Test
        void testGetAllUserAccounts_SuccessAdminFetchesAll(){
            User admin = new User("Admin","admin","admin123");
            admin.changeRole(Role.ADMIN);
            Account acc1 = new Account();
            Account acc2 = new Account();
            Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
            Page<Account> page = new PageImpl<>(List.of(testAccount,acc1,acc2));

            when(repoU.findById(23L)).thenReturn(Optional.of(admin));
            when(repoA.findAll(pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount(23L,0,5,"id","desc");

            assertNotNull(dto);
            assertEquals(3,dto.getContent().size(),"should contain one Account");
        }

        @Test
        void testGetAllUserAccounts_FailUserNotFound(){

            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.getAllUserAccount(11L,0,5,"id","desc");
            });

            assertTrue(ex.getMessage().contains("11"));
        }

        @Test
        void testGetAllUserAccounts_NoAccountsFound(){

            Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
            Page<Account> page = new PageImpl<>(List.of());

            when(repoU.findById(11L)).thenReturn(Optional.of(testUser));
            when(repoA.findByUserId(11L,pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount(11L,0,5,"id","desc");

            assertTrue(dto.isEmpty());
        }



    }

}
