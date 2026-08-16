package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.account.AccountDto;
import com.nate.bankingsystemapi.dto.account.LockedAccounts;
import com.nate.bankingsystemapi.dto.account.PostAccountDto;
import com.nate.bankingsystemapi.exception.AccountNotFoundException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.account.entity.TestAccount;
import com.nate.bankingsystemapi.model.account.enums.CurrencyCode;
import com.nate.bankingsystemapi.model.user.entity.TestUser;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.service.account.AccountService;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    private Account testAccount2;
    private User testUser2;



    @BeforeEach
    void startUp(){
        testUser = new TestUser(1L,"Tester","test","hash-pass");
        testUser2 = new TestUser(2L,"Tester2","test2","hash-pass");
        testAccount = new TestAccount(2L,testUser, CurrencyCode.ZAR);
        testAccount2 = new TestAccount(3L,testUser2, CurrencyCode.ZAR);
        testAccount.credit(BigDecimal.valueOf(40000L));
        testPost = new PostAccountDto(CurrencyCode.ZAR.toString());

    }


    @DisplayName("Testing Create Account: All possible results")
    @Nested
    class CreateAccount {
        @Test
        void testCreateAccount_Success() {
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.save(any(Account.class))).thenReturn(testAccount);

            AccountDto dto = service.createAccount(testPost, "test");

            assertEquals(testPost.getCurrency(), dto.getCurrency(), "Currency code should be the same");

            verify(repoA).save(any(Account.class));

        }

        @Test
        void testCreateAccount_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.createAccount(testPost,"test");
            });

            assertTrue(ex.getMessage().contains(testUser.getUsername()));
        }

    }

    @DisplayName("Testing get account by id: All possible results")
    @Nested
    class GetAccountById{
        @Test
        void testGetAccountById_Success(){
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.findByIdAndUserId(2L,testUser.getId())).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountById(2L,"test");

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUser().getId(),dto.getUserId(),"should have the same user ID");
            assertEquals(CurrencyCode.ZAR.toString(),dto.getCurrency(),"should have the same currency code");

            verify(repoA).findByIdAndUserId(2L,testUser.getId());
        }


        @Test
        void testGetAccountById_FailUserNotFound(){
            Exception ex = assertThrows(UserNotFoundException.class,()->{
                service.getAccountById(testAccount.getId(),"test");
            });

            assertTrue(ex.getMessage().contains("test"));
        }

        @Test
        void testGetAccountById_FailAccountNotFound(){
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

            Exception ex = assertThrows(AccountNotFoundException.class,()->{
                service.getAccountById(testAccount.getId(),"test");
            });

            assertTrue(ex.getMessage().contains(testAccount.getId().toString()));
        }


    }

    @DisplayName("Testing get account by id: All possible results")
    @Nested
    class GetAccountByAccountNum{
        @Test
        void testGetAccountByAccountNum_Success(){
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.findByAccountNum(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));

            AccountDto dto = service.getAccountByAccountNumber(testAccount.getAccountNum(),"test");

            assertNotNull(dto);

            assertEquals(testAccount.getBalance(),dto.getBalance(),"should have the same balance");
            assertEquals(testAccount.getUser().getId(),dto.getUserId(),"should have the same user ID");
        }


        @Test
        void testGetAccountByAccountNum_FailUserNotFound(){
            assertThrows(UserNotFoundException.class,()->{
                service.getAccountByAccountNumber(testAccount.getAccountNum(),"test");
            });
        }

        @Test
        void testGetAccountByAccountNum_FailAccountNotFound(){
            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            assertThrows(AccountNotFoundException.class,()->{
                service.getAccountByAccountNumber(3654987L,"test");
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

            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoA.findByUser(testUser,pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount("test",0,5,"id","desc");

            assertNotNull(dto);
            assertEquals(1,dto.getContent().size(),"should contain one Account");
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

            when(repoU.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
            when(repoA.existsByUser(testUser)).thenReturn(Boolean.TRUE);
            when(repoA.findByUser(testUser,pageable)).thenReturn(page);

            Page<AccountDto> dto = service.getAllUserAccount("test",0,5,"id","desc");

            assertTrue(dto.isEmpty());
        }



    }

    @Nested
    class testTransfer{
        @Test
        void testTransfer_Success(){
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));

            LockedAccounts accounts = service.transfer(testAccount.getAccountNum(),testAccount2.getAccountNum(),testUser.getId(),BigDecimal.valueOf(5000));

            assertNotNull(accounts);

            assertEquals(BigDecimal.valueOf(35000),testAccount.getBalance(),"should have that balance after deduction");
            assertEquals(BigDecimal.valueOf(5000),testAccount2.getBalance(),"should have that balance after credit");

            verify(repoA).findByAccountNumForUpdate(testAccount.getAccountNum());
            verify(repoA).findByAccountNumForUpdate(testAccount2.getAccountNum());

        }

        @Test
        void shouldFailTransfer_FromAccountNumNull(){

            assertThrows(IllegalArgumentException.class,()->{
                service.transfer(null,testAccount2.getAccountNum(),testUser.getId(),BigDecimal.valueOf(5000));
            });
        }

        @Test
        void shouldFailTransfer_ToAccountNumNull(){

            assertThrows(IllegalArgumentException.class,()->{
                service.transfer(testAccount.getAccountNum(),null,testUser.getId(),BigDecimal.valueOf(5000));
            });
        }


        @Test
        void shouldFailTransfer_TransferToSameAccount(){

            assertThrows(IllegalArgumentException.class,()->{
                service.transfer(testAccount.getAccountNum(),testAccount.getAccountNum(),testUser.getId(),BigDecimal.valueOf(5000));
            });
        }

        @Test
        void shouldFailTransfer_FromAccountNotFound(){

            assertThrows(AccountNotFoundException.class,()->{
                service.transfer(testAccount.getAccountNum(),testAccount2.getAccountNum(),testUser.getId(),BigDecimal.valueOf(5000));
            });
        }

        @Test
        void shouldFailTransfer_ToAccountNotFound(){
            when(repoA.findByAccountNumForUpdate(testAccount.getAccountNum())).thenReturn(Optional.of(testAccount));
            assertThrows(AccountNotFoundException.class,()->{
                service.transfer(testAccount.getAccountNum(),15236486626L,testUser.getId(),BigDecimal.valueOf(5000));
            });
        }

        @Test
        void shouldFailTransfer_AccessDenied(){
            User invalidUser = new TestUser(33L,"Invalid","invalidTester","hash-pass");
            Account account = new TestAccount(55L,invalidUser,CurrencyCode.ZAR);

            when(repoA.findByAccountNumForUpdate(account.getAccountNum())).thenReturn(Optional.of(account));
            when(repoA.findByAccountNumForUpdate(testAccount2.getAccountNum())).thenReturn(Optional.of(testAccount2));

            assertThrows(AccessDeniedException.class,()->{
                service.transfer(account.getAccountNum(),testAccount2.getAccountNum(),testUser.getId(),BigDecimal.valueOf(5000));
            });
        }

    }

    @Nested
    class Admin{
        @Test
        void  testGetAllUserAccounts_SuccessAdminFetchesAll(){
            Account acc1 = Account.create(testUser,CurrencyCode.ZAR);
            Account acc2 = Account.create(testUser,CurrencyCode.ZAR);
            Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
            Page<Account> page = new PageImpl<>(List.of(testAccount,acc1,acc2));

            when(repoU.existsByUsernameAndRole("admin", Role.ADMIN)).thenReturn(true);
            when(repoA.findAll(pageable)).thenReturn(page);

            Page<AccountDto> dto = service.adminGetAllUserAccount("admin",0,5,"id","desc");

            assertNotNull(dto);
            assertEquals(3,dto.getContent().size(),"should contain one Account");
        }

        @Test
        void shouldFailGetAllUserAccountForAdmin_AdminNotFound(){
           Exception e = assertThrows(AccessDeniedException.class,()->{
                service.adminGetAllUserAccount("tester",0,5,"id","desc");
            });

            assertTrue(e.getMessage().contains("tester"));
        }

        @Test
        void shouldGetAccountByIdForAdmin(){
            when(repoU.existsByUsernameAndRole("admin",Role.ADMIN)).thenReturn(true);
            when(repoA.findById(2L)).thenReturn(Optional.of(testAccount));

            AccountDto accountDto = service.adminGetAccountById(2L,"admin");

            assertNotNull(accountDto);

            assertEquals(2L,accountDto.getId());
            assertEquals(testUser.getId(), accountDto.getUserId());
            assertEquals(CurrencyCode.ZAR.toString(),accountDto.getCurrency());


            verify(repoA).findById(2L);

        }

        @Test
        void shouldFailGetAccountByIdForAdmin_AdminNotFound(){

            Exception e = assertThrows(AccessDeniedException.class,()->{
                service.adminGetAccountById(2L,"test");
            });

            assertTrue(e.getMessage().contains("test"));
        }

        @Test
        void shouldFailGetAccountByIdForAdmin_AccountNotFound(){
            when(repoU.existsByUsernameAndRole("admin",Role.ADMIN)).thenReturn(true);

            Exception e = assertThrows(AccountNotFoundException.class,()->{
                service.adminGetAccountById(2L,"admin");
            });

            assertTrue(e.getMessage().contains("2"));

        }

        @Test
        void shouldGetAccountByAccountNumForAdmin(){
            Long accNum = testAccount.getAccountNum();
            when(repoU.existsByUsernameAndRole("admin",Role.ADMIN)).thenReturn(true);
            when(repoA.findByAccountNum(accNum)).thenReturn(Optional.of(testAccount));

            AccountDto accountDto = service.adminGetAccountByAccountNumber(accNum,"admin");

            assertNotNull(accountDto);

            assertEquals(accNum,accountDto.getAccountNum());
            assertEquals(testUser.getId(),accountDto.getUserId());
            assertEquals(CurrencyCode.ZAR.toString(),accountDto.getCurrency());
            assertEquals(testAccount.getId(),accountDto.getId());


            verify(repoA).findByAccountNum(accNum);
        }
    }

}
