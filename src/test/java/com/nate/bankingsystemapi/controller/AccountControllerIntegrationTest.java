package com.nate.bankingsystemapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nate.bankingsystemapi.dto.account.AccountDto;
import com.nate.bankingsystemapi.dto.account.PostAccountDto;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.account.enums.CurrencyCode;
import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.security.JwtService;
import com.nate.bankingsystemapi.service.account.IAccountService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AccountControllerIntegrationTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository repo;
    @Autowired
    private AccountRepository repoA;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private IAccountService accountService;

    private String tokenTestUser;
    private String tokenAdminUser;
    private Account testAccount;


    @BeforeEach
    void startUp(){
        repo.deleteAll();
        User testUser = User.createUser("Tester","tester",encoder.encode("test123"));
        User adminUser = User.createUser("Admin","admin",encoder.encode("test123"));
        adminUser.changeRole(Role.ADMIN);
        repo.save(testUser);
        repo.save(adminUser);

        tokenTestUser = jwtService.generateToken(testUser);
        tokenAdminUser = jwtService.generateToken(adminUser);

        Account acc = Account.create(testUser, CurrencyCode.ZAR);
        testAccount = repoA.save(acc);

    }

    @DisplayName("Testing account creations: All possible results")
    @Nested
    class CreateAccount {
        @Test
        void testCreateAccount_Success() throws Exception {
            PostAccountDto dto = new PostAccountDto(CurrencyCode.EUR.toString());

            mvc.perform(post("/api/v1/accounts")
                            .header("Authorization", "Bearer " + tokenTestUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.balance").value(0))
                    .andExpect(jsonPath("$.currency").value(CurrencyCode.EUR.toString()));


        }


        @Test
        void testCreateAccount_FailBadRequestNoBalance() throws Exception {
            PostAccountDto dto = new PostAccountDto(null);

            mvc.perform(post("/api/v1/accounts")
                            .header("Authorization", "Bearer " + tokenTestUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());

        }



    }

    @DisplayName("Testing get Account by Id: Testing all possible outcomes")
    @Nested
    class GetById{
        @Test
        void testGetById_Success() throws Exception {
            Long id = testAccount.getId();
            mvc.perform(get("/api/v1/accounts/fetch/" + id)
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(new BigDecimal("0.0")))
                    .andExpect(jsonPath("$.currency").value(testAccount.getCurrency().toString()))
                    .andExpect(jsonPath("$.accountNum").value(testAccount.getAccountNum()))
                    .andExpect(jsonPath("$.userId").value(testAccount.getUser().getId()));
        }

        @Test
        void testGetById_FailNotFound() throws Exception {
            mvc.perform(get("/api/v1/accounts/fetch/11")
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isNotFound());
        }

        @Test
        void testGetById_FailUnAuthorized() throws Exception {
            Long id = testAccount.getId();
            mvc.perform(get("/api/v1/accounts/fetch/"+id))
                    .andExpect(status().isUnauthorized());
        }

    }

    @DisplayName("Testing get Account by account number: Testing all possible outcomes")
    @Nested
    class GetByAccountNum{
        @Test
        void testGetByAccountNum_Success() throws Exception {
            Long accountNum = testAccount.getAccountNum();
            mvc.perform(get("/api/v1/accounts/accountNum/"+accountNum)
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(new BigDecimal("0.0")))
                    .andExpect(jsonPath("$.currency").value(testAccount.getCurrency().toString()))
                    .andExpect(jsonPath("$.id").value(testAccount.getId()))
                    .andExpect(jsonPath("$.userId").value(testAccount.getUser().getId()));
        }



        @Test
        void testGetByAccountNum_FailNotFound() throws Exception {
            mvc.perform(get("/api/v1/accounts/accountNum/00569875")
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isNotFound());
        }

        @Test
        void testGetByAccountNum_FailUnAuthorized() throws Exception {
            String accountNum = testAccount.getAccountNum().toString();
            mvc.perform(get("/api/v1/accounts/accountNum/"+ accountNum))
                    .andExpect(status().isUnauthorized());
        }

    }

    @DisplayName("Testing Get All User Accounts: Testing all possible results")
    @Nested
    class GetAllUserAccounts{
        @Test
        void testGetAllUserAccounts_Success() throws Exception {
            mvc.perform(get("/api/v1/accounts")
                            .param("page","0")
                            .param("size","5")
                            .param("sortBy","id")
                            .param("direction","desc")
                            .header("Authorization", "Bearer "+tokenTestUser))
                    .andExpect(status().isOk());
        }

        @Test
        void testGetAllUserAccounts_FailUnauthorized() throws Exception {
            mvc.perform(get("/api/v1/accounts")
                            .param("page","0")
                            .param("size","5")
                            .param("sortBy","id")
                            .param("direction","desc"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class Admin {
        @Test
        void shouldGetAllAccountsForAdmin() throws Exception {
            mvc.perform(get("/api/v1/admin/accounts/fetch")
                            .param("page","0")
                            .param("size","5")
                            .param("sortBy","id")
                            .param("direction","desc")
                            .header("Authorization", "Bearer "+tokenAdminUser))
                    .andExpect(status().isOk());

            String username = jwtService.extractUsername(tokenAdminUser);

            Page<AccountDto> responses  = accountService.adminGetAllUserAccount(username,0,5,"id","desc");

            assertNotNull(responses);

            assertEquals(1, responses.getTotalElements());

        }

        @Test
        void shouldFailGetAllAccountForAdmin_AdminNotFound() throws Exception {
             mvc.perform(get("/api/v1/admin/accounts/fetch")
                             .param("page","0")
                             .param("size","5")
                             .param("sortBy","id")
                             .param("direction","desc"))
                     .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldGetAccountByIdForAdmin() throws Exception {
            Long id = testAccount.getId();
            mvc.perform(get("/api/v1/admin/accounts/fetch/" +id)
                    .header("Authorization","Bearer " + tokenAdminUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNum").value(testAccount.getAccountNum()))
                    .andExpect(jsonPath("$.userId").value(testAccount.getUser().getId()))
                    .andExpect(jsonPath("$.balance").value(new BigDecimal("0.0")))
                    .andExpect(jsonPath("$.currency").value(testAccount.getCurrency().toString()))
                    .andExpect(jsonPath("$.id").value(testAccount.getId()));
        }


        @Test
        void shouldFailGetAccountByIdForAdmin_AccountNotFound() throws Exception {
            mvc.perform(get("/api/v1/admin/accounts/fetch/113")
                    .header("Authorization","Bearer " + tokenAdminUser))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldFailGetAccountByIdForAdmin_NotAdmin() throws Exception {
            mvc.perform(get("/api/v1/admin/accounts/fetch/113")
                            .header("Authorization","Bearer " + tokenTestUser))
                    .andExpect(status().isForbidden());
        }

        @Test
        void shouldFailGetAccountByIdForAdmin_UnAuthorized() throws Exception {
            mvc.perform(get("/api/v1/admin/accounts/fetch/113"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void shouldGetAccountByAccountNumForAdmin() throws Exception {
            Long accountNum = testAccount.getAccountNum();
            mvc.perform(get("/api/v1/admin/accounts/fetch/accNum/"+accountNum)
                    .header("Authorization","Bearer " + tokenAdminUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accountNum").value(testAccount.getAccountNum()))
                    .andExpect(jsonPath("$.userId").value(testAccount.getUser().getId()))
                    .andExpect(jsonPath("$.balance").value(new BigDecimal("0.0")))
                    .andExpect(jsonPath("$.currency").value(testAccount.getCurrency().toString()))
                    .andExpect(jsonPath("$.id").value(testAccount.getId()));
        }

        @Test
        void shouldFailGetAccountByAccountNumForAdmin_AccountNotFound() throws Exception {
            mvc.perform(get("/api/v1/admin/accounts/fetch/accNum/26485158")
                    .header("Authorization", "Bearer " + tokenAdminUser))
                    .andExpect(status().isNotFound());
        }

        @Test
        void shouldFailGetAccountByAccountNumForAdmin_Unauthorized() throws Exception {
            Long acc = testAccount.getAccountNum();
            mvc.perform(get("/api/v1/admin/accounts/fetch/accNum/" +acc)
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isForbidden());
        }


        @Test
        void shouldFailGetAccountByAccountNumForAdmin_NoAdminFound() throws Exception {
            Long acc = testAccount.getAccountNum();
            mvc.perform(get("/api/v1/admin/accounts/fetch/accNum/" +acc))
                    .andExpect(status().isUnauthorized());
        }



    }
}
