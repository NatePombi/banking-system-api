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
        testAccount = new TestAccount(2L,testUser);
        testAccount.changeBalance(40000L);
        testPost = new PostAccountDto(40000L);

    }


    @DisplayName("Testing Create Account: All possible results")
    @Nested
    class CreateAccount {
        @Test
        void testCreateAccount_Success() {
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.save(any(Account.class))).thenReturn(testAccount);

            AccountDto dto = service.createAccount(testPost, testUser);

            assertEquals(testPost.getBalance(), dto.getBalance(), "balance should the same");

        }

        @Test
        void testCreateAccount_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.createAccount(testPost,testUser);
            });

            assertTrue(ex.getMessage().contains(testUser.getUsername()));
        }

    }

    @DisplayName("Testing get account by id: All possible results")
    @Nested
    class GetAccountById{
        @Test
        void testGetAccountById_Success(){
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountById(2L,testUser);

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUser().getId(),dto.getUserId(),"should have the same user ID");
        }

        @Test
        void testGetAccountById_SuccessEvenIfNotOwner_Admin(){
            User admin = User.createUser("Admin","admin","admin123");
            admin.changeRole(Role.ADMIN);
            when(repoU.existsByIdAndUsername(admin.getId(),admin.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountById(2L,admin);

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUser().getId(),dto.getUserId(),"should have the same user ID");
        }

        @Test
        void testGetAccountById_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.getAccountById(testAccount.getId(),testUser);
            });

            assertTrue(ex.getMessage().contains(testUser.getUsername()));
        }

        @Test
        void testGetAccountById_FailAccountNotFound(){
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);

            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.getAccountById(testAccount.getId(),testUser);
            });

            assertTrue(ex.getMessage().contains(testAccount.getId().toString()));
        }

        @Test
        void testGetAccountById_FailUnauthorized(){
            User testUser2 = new TestUser(11L,"Mark","mark","mark123");
            when(repoU.existsByIdAndUsername(testUser2.getId(),testUser2.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));
            assertThrows(AccessDeniedException.class,()->{
                service.getAccountById(testAccount.getId(),testUser2);
            });

        }

    }

    @DisplayName("Testing get account by id: All possible results")
    @Nested
    class GetAccountByAccountNum{
        @Test
        void testGetAccountByAccountNum_Success(){
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNum(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountByAccountNumber(testAccount.getAccountNum(),testUser);

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUser().getId(),dto.getUserId(),"should have the same user ID");
        }

        @Test
        void testGetAccountByAccountNum_SuccessEvenIfNotOwner_Admin(){
            User admin = new TestUser(22L,"Admin","admin","admin123");
            admin.changeRole(Role.ADMIN);
            when(repoU.existsByIdAndUsername(admin.getId(),admin.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.findByAccountNum(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountByAccountNumber(testAccount.getAccountNum(),admin);

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUser().getId(),dto.getUserId(),"should have the same user ID");
        }

        @Test
        void testGetAccountByAccountNum_FailUserNotFound(){
            assertThrows(UserNotFoundException.class,()->{
                service.getAccountByAccountNumber(testAccount.getAccountNum(),testUser);
            });
        }

        @Test
        void testGetAccountByAccountNum_FailAccountNotFound(){
            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            assertThrows(AccountNotFoundException.class,()->{
                service.getAccountByAccountNumber(3654987L,testUser);
            });
        }

        @Test
        void testGetAccountByAccountNum_FailUnauthorized(){
            User testUser2 = new TestUser(33L,"Mark","mark","mark123");
            when(repoA.findByAccountNum(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoU.existsByIdAndUsername(testUser2.getId(),testUser2.getUsername())).thenReturn(Boolean.TRUE);

            assertThrows(AccessDeniedException.class,()->{
                service.getAccountByAccountNumber(testAccount.getAccountNum(),testUser2);
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

            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoA.findByUser(testUser,pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount(testUser,0,5,"id","desc");

            assertNotNull(dto);
            assertEquals(1,dto.getContent().size(),"should contain one Account");
        }


        @Test
        void  testGetAllUserAccounts_SuccessAdminFetchesAll(){
            User admin = new TestUser(13L,"Admin","admin","admin123");
            admin.changeRole(Role.ADMIN);
            Account acc1 = new Account(testUser);
            Account acc2 = new Account(testUser);
            Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
            Page<Account> page = new PageImpl<>(List.of(testAccount,acc1,acc2));

            when(repoU.existsByIdAndUsername(admin.getId(),admin.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.existsByUser(admin)).thenReturn(Boolean.TRUE);
            when(repoA.findAll(pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount(admin,0,5,"id","desc");

            assertNotNull(dto);
            assertEquals(3,dto.getContent().size(),"should contain one Account");
        }

        @Test
        void testGetAllUserAccounts_FailUserNotFound(){

            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.getAllUserAccount(testUser,0,5,"id","desc");
            });

            assertTrue(ex.getMessage().contains(testUser.getUsername()));
        }

        @Test
        void testGetAllUserAccounts_NoAccountsFound(){

            Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
            Page<Account> page = new PageImpl<>(List.of());

            when(repoU.existsByIdAndUsername(testUser.getId(),testUser.getUsername())).thenReturn(Boolean.TRUE);
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoA.findByUser(testUser,pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount(testUser,0,5,"id","desc");

            assertTrue(dto.isEmpty());
        }



    }

}
