package com.nate.bankingsystemapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nate.bankingsystemapi.dto.transaction.FundsRequest;
import com.nate.bankingsystemapi.dto.transaction.TransferRequest;
import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.account.enums.CurrencyCode;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.repository.*;
import com.nate.bankingsystemapi.security.JwtService;
import com.nate.bankingsystemapi.service.transaction.TransactionCreationService;
import com.nate.bankingsystemapi.service.transaction.TransactionFailureService;
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

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionsIntegrationTest {

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private AccountRepository repoA;
    @Autowired
    private UserRepository repoU;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionFailureService transactionFailureService;
    @Autowired
    private TransactionCreationService transactionCreationService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtService jwtService;

    private String token;
    private Account testAccount1;
    private Account testAccount2;
    private String reqID;
    @BeforeEach
    void startUp(){
        User testUser = User.createUser("Tester","tester",encoder.encode("test123"));
        User testUser2 = User.createUser("Tester2","tester2",encoder.encode("test123"));

        repoU.save(testUser);
        repoU.save(testUser2);

        token = jwtService.generateToken(testUser);

         testAccount1 = Account.create(testUser, CurrencyCode.ZAR);
         testAccount1.credit(BigDecimal.valueOf(10000));
         testAccount2 = Account.create(testUser2,CurrencyCode.ZAR);

        repoA.save(testAccount1);
        repoA.save(testAccount2);

        reqID = UUID.randomUUID().toString();
    }

    @Nested
    class Transfer {
        @Test
        void testTransferFunds_Success() throws Exception {
            TransferRequest dto = new TransferRequest(testAccount1.getAccountNum(), testAccount2.getAccountNum(), new BigDecimal("2000.00"),reqID);

            mvc.perform(post("/api/v1/transactions/transfer")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isOk());

            repoA.findByAccountNum(testAccount1.getAccountNum()).ifPresent(acc -> {
                assertEquals(new BigDecimal("8000.00"), acc.getBalance(), "Should have a decreased balance by transfer amount");
            });

            repoA.findByAccountNum(testAccount2.getAccountNum()).ifPresent(acc -> {
                assertEquals(new BigDecimal("2000.00"), acc.getBalance(), "Should have an increased balance by transfer amount");
            });
        }

        @Test
        void shouldFailTransferFunds_DuplicateRequest() throws Exception {
            TransferRequest dto = new TransferRequest(testAccount1.getAccountNum(), testAccount2.getAccountNum(), BigDecimal.valueOf(2000),reqID);
            Transactions transactions = Transactions.create(reqID);
            transactionRepository.save(transactions);

            mvc.perform(post("/api/v1/transactions/transfer")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }

        @Test
        void testTransferFunds_Fail_InsufficientFunds() throws Exception {
            TransferRequest dto = new TransferRequest(testAccount1.getAccountNum(),testAccount2.getAccountNum(), new BigDecimal("20000.00"),reqID);

            mvc.perform(post("/api/v1/transactions/transfer")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());

            repoA.findByAccountNum(testAccount1.getAccountNum()).ifPresent(acc -> {
                assertEquals(new BigDecimal("10000.00"), acc.getBalance(), "Funds should stay the same");
            });

            repoA.findByAccountNum(testAccount2.getAccountNum()).ifPresent(acc -> {
                assertEquals(new BigDecimal("0.00"), acc.getBalance(), "funds should stay the same");
            });
        }


    }

    @Nested
    class Deposit{

        @Test
        void testDepositFunds_Success() throws Exception {
            BigDecimal amount = new BigDecimal("2000.00");
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),amount,reqID);
            mvc.perform(post("/api/v1/transactions/deposit")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            repoA.findByAccountNum(testAccount1.getAccountNum()).ifPresent(acc ->{
                assertEquals(testAccount1.getBalance().add(amount), acc.getBalance(),"Current balance should be the sum of previous balance and the amount");
            });
        }

        @Test
        void shouldFailTestFund_FailDuplicateRequest() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(2000);
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),amount,reqID);
            Transactions transactions = Transactions.create(reqID);
            transactionRepository.save(transactions);

            mvc.perform(post("/api/v1/transactions/deposit")
                    .header("Authorization","Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());
        }

        @Test
        void testDepositFunds_FailBadRequestNoAmount() throws Exception {
            FundsRequest req = new FundsRequest(testAccount2.getAccountNum(),null,reqID);

            mvc.perform(post("/api/v1/transactions/deposit")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testDepositFunds_FailBadRequestNoRequestID() throws Exception {
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),BigDecimal.valueOf(10),null);
            req.setRequestID(null);

            mvc.perform(post("/api/v1/transactions/deposit")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }


        @Test
        void testDepositFunds_FailBadRequestNoAccountAccountNum() throws Exception {
            FundsRequest req = new FundsRequest(null,BigDecimal.valueOf(5000),reqID);

            mvc.perform(post("/api/v1/transactions/deposit")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testDepositFuds_FailUnauthorized() throws Exception{
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            mvc.perform(post("/api/v1/transactions/deposit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());

        }
    }

    @Nested
    class Withdraw{

        @Test
        void testWithdrawFunds_Success() throws Exception {
            BigDecimal amount = new BigDecimal("2000.00");
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),amount,reqID);
            mvc.perform(post("/api/v1/transactions/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isOk());

            repoA.findByAccountNum(testAccount1.getAccountNum()).ifPresent(acc ->{
                assertEquals(testAccount1.getBalance().subtract(amount), acc.getBalance(),"Current balance should be the difference between the  previous balance and the amount");
            });
        }

        @Test
        void shouldFailWithdrawFunds_DuplicateRequest() throws Exception {
            BigDecimal amount = BigDecimal.valueOf(2000);
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),amount,reqID);

            Transactions transactions = Transactions.create(reqID);
            transactionRepository.save(transactions);

            mvc.perform(post("/api/v1/transactions/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isConflict());



        }

        @Test
        void testWithdrawFunds_FailBadRequestInsufficientBalance() throws Exception {
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),BigDecimal.valueOf(200000),reqID);

            mvc.perform(post("/api/v1/transactions/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testWithdrawFunds_FailBadRequestNoAmount() throws Exception {
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),null,reqID);

            mvc.perform(post("/api/v1/transactions/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testWithdrawFunds_FailBadRequestNoRequestID() throws Exception {
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),BigDecimal.valueOf(10),reqID);
            req.setRequestID(null);

            mvc.perform(post("/api/v1/transactions/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }


        @Test
        void testWithdrawFunds_FailBadRequestNoAccountId() throws Exception {
            FundsRequest req = new FundsRequest(null,BigDecimal.valueOf(5000),reqID);

            mvc.perform(post("/api/v1/transactions/withdraw")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void testWithdrawFunds_FailUnauthorized() throws Exception{
            FundsRequest req = new FundsRequest(testAccount1.getAccountNum(),BigDecimal.valueOf(5000),reqID);

            mvc.perform(post("/api/v1/transactions/withdraw")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());

        }
    }


}
