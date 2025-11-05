package com.nate.bankingsystemapi.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransferRequest;
import com.nate.bankingsystemapi.model.Account;
import com.nate.bankingsystemapi.model.Role;
import com.nate.bankingsystemapi.model.User;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionIntegrationTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private AccountRepository repoA;
    @Autowired
    private UserRepository repoU;
    @Autowired
    private PasswordEncoder encoder;

    private String token;
    private Account testAccount1;
    private Account testAccount2;
    @BeforeEach
    void startUp(){
        User testUser = new User(null,"Tester","tester",encoder.encode("test123"), Role.USER);
        repoU.save(testUser);

        token = JwtUtil.generateToken(testUser.getUsername(),testUser.getRole());

         testAccount1 = new Account(null,10000L,"USD",testUser,0);
         testAccount2 = new Account(null,0L,"USD",testUser,0);

        repoA.save(testAccount1);
        repoA.save(testAccount2);
    }

    @Nested
    class Transfer {
        @Test
        void testTransferFunds_Success() throws Exception {
            TransferRequest dto = new TransferRequest(1L, 2L, 2000L);

            mvc.perform(post("/transaction/transfer")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            repoA.findById(1L).ifPresent(acc -> {
                assertEquals(8000L, acc.getBalance(), "Should have a decreased balance by transfer amount");
            });

            repoA.findById(2L).ifPresent(acc -> {
                assertEquals(2000L, acc.getBalance(), "Should have an increased balance by transfer amount");
            });
        }

        @Test
        void testTransferFunds_Fail_InsufficientFunds() throws Exception {
            TransferRequest dto = new TransferRequest(1L, 2L, 20000L);

            mvc.perform(post("/transaction/transfer")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());

            repoA.findById(1L).ifPresent(acc -> {
                assertEquals(10000L, acc.getBalance(), "Funds should stay the same");
            });

            repoA.findById(2L).ifPresent(acc -> {
                assertEquals(0L, acc.getBalance(), "funds should stay the same");
            });
        }


    }

    @Nested
    class Deposit{

        @Test
        void testDepositFunds_Success() throws Exception {
            Long amount = 2000L;
            FundsRequest req = new FundsRequest(1L,amount);
            mvc.perform(post("/transaction/deposit")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            repoA.findById(1L).ifPresent(acc ->{
                assertEquals(testAccount1.getBalance() + amount, acc.getBalance(),"Current balance should be the sum of previous balance and the amount");
            });
        }

        @Test
        void testDepositFunds_FailBadRequestNoAmount() throws Exception {
            FundsRequest req = new FundsRequest(1L,null);

            mvc.perform(post("/transaction/deposit")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }


        @Test
        void testDepositFunds_FailBadRequestNoAccountId() throws Exception {
            FundsRequest req = new FundsRequest(null,5000L);

            mvc.perform(post("/transaction/deposit")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testDepositFuds_FailUnauthorized() throws Exception{
            FundsRequest req = new FundsRequest(1L,5000L);

            mvc.perform(post("/transaction/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());

        }
    }

    @Nested
    class Withdraw{

        @Test
        void testWithdrawFunds_Success() throws Exception {
            Long amount = 2000L;
            FundsRequest req = new FundsRequest(1L,amount);
            mvc.perform(post("/transaction/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            repoA.findById(1L).ifPresent(acc ->{
                assertEquals(testAccount1.getBalance() - amount, acc.getBalance(),"Current balance should be the difference between the  previous balance and the amount");
            });
        }

        @Test
        void testWithdrawFunds_FailBadRequestInsufficientBalance() throws Exception {
            FundsRequest req = new FundsRequest(1L,200000L);

            mvc.perform(post("/transaction/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testWithdrawFunds_FailBadRequestNoAmount() throws Exception {
            FundsRequest req = new FundsRequest(1L,null);

            mvc.perform(post("/transaction/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }


        @Test
        void testWithdrawFunds_FailBadRequestNoAccountId() throws Exception {
            FundsRequest req = new FundsRequest(null,5000L);

            mvc.perform(post("/transaction/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testWithdrawFunds_FailUnauthorized() throws Exception{
            FundsRequest req = new FundsRequest(1L,5000L);

            mvc.perform(post("/transaction/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());

        }
    }


}
