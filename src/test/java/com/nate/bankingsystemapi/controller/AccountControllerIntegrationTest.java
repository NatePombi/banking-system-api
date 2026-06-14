package com.nate.bankingsystemapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import com.nate.bankingsystemapi.model.Account;
import com.nate.bankingsystemapi.model.Role;
import com.nate.bankingsystemapi.model.User;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.security.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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
        tokenAdminUser = jwtService.generateToken(testUser);

        testAccount = new Account(testUser);
        repoA.save(testAccount);

    }

    @DisplayName("Testing account creations: All possible results")
    @Nested
    class CreateAccount {
        @Test
        void testCreateAccount_Success() throws Exception {
            PostAccountDto dto = new PostAccountDto(1000L);

            mvc.perform(post("/account")
                            .header("Authorization", "Bearer " + tokenTestUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.balance").value(1000))
                    .andExpect(jsonPath("$.currency").value("ZAR"));


        }


        @Test
        void testCreateAccount_FailBadRequestNoBalance() throws Exception {
            PostAccountDto dto = new PostAccountDto(null);

            mvc.perform(post("/account")
                            .header("Authorization", "Bearer " + tokenTestUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isUnprocessableEntity());

        }

        @Test
        void testCreateAccount_FailBadRequestBalanceNegative() throws Exception {
            PostAccountDto dto = new PostAccountDto(-7000L);

            mvc.perform(post("/account")
                            .header("Authorization", "Bearer " + tokenTestUser)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isUnprocessableEntity());

        }

    }

    @DisplayName("Testing get Account by Id: Testing all possible outcomes")
    @Nested
    class GetById{
        @Test
        void testGetById_Success() throws Exception {
            mvc.perform(get("/account/1")
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(0))
                    .andExpect(jsonPath("$.currency").value("ZAR"));
        }

        @Test
        void testGetById_SuccessAdmin() throws Exception {
            mvc.perform(get("/account/1")
                            .header("Authorization", "Bearer " + tokenAdminUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(0))
                    .andExpect(jsonPath("$.currency").value("ZAR"));
        }

        @Test
        void testGetById_FailNotFound() throws Exception {
            mvc.perform(get("/account/11")
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isNotFound());
        }

        @Test
        void testGetById_FailUnAuthorized() throws Exception {
            mvc.perform(get("/account/1"))
                    .andExpect(status().isUnauthorized());
        }

    }

    @DisplayName("Testing get Account by account number: Testing all possible outcomes")
    @Nested
    class GetByAccountNum{
        @Test
        void testGetByAccountNum_Success() throws Exception {
            String accountNum = testAccount.getAccountNum().toString();
            mvc.perform(get("/account/accountNum/"+accountNum)
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(0))
                    .andExpect(jsonPath("$.currency").value("ZAR"));
        }

        @Test
        void testGetByAccountNum_SuccessAdmin() throws Exception {
            String accountNum = testAccount.getAccountNum().toString();
            mvc.perform(get("/account/accountNum/"+accountNum)
                            .header("Authorization", "Bearer " + tokenAdminUser))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.balance").value(0))
                    .andExpect(jsonPath("$.currency").value("ZAR"));
        }

        @Test
        void testGetByAccountNum_FailNotFound() throws Exception {
            mvc.perform(get("/account/accountNum/00569875")
                            .header("Authorization", "Bearer " + tokenTestUser))
                    .andExpect(status().isNotFound());
        }

        @Test
        void testGetByAccountNum_FailUnAuthorized() throws Exception {
            String accountNum = testAccount.getAccountNum().toString();
            mvc.perform(get("/account/accountNum/"+ accountNum))
                    .andExpect(status().isUnauthorized());
        }

    }

    @DisplayName("Testing Get All User Accounts: Testing all possible results")
    @Nested
    class GetAllUserAccounts{
        @Test
        void testGetAllUserAccounts_Success() throws Exception {
            mvc.perform(get("/account")
                            .param("page","0")
                            .param("size","5")
                            .param("sortBy","id")
                            .param("direction","desc")
                            .header("Authorization", "Bearer "+tokenTestUser))
                    .andExpect(status().isOk());
        }

        @Test
        void testGetAllUserAccounts_FailUnauthorized() throws Exception {
            mvc.perform(get("/account")
                            .param("page","0")
                            .param("size","5")
                            .param("sortBy","id")
                            .param("direction","desc"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
