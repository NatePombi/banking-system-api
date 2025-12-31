package com.nate.bankingsystemapi.concurrentTests;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransferRequest;
import com.nate.bankingsystemapi.model.Account;
import com.nate.bankingsystemapi.model.Role;
import com.nate.bankingsystemapi.model.User;
import com.nate.bankingsystemapi.repository.AccountRepository;
import com.nate.bankingsystemapi.repository.LedgerEntryRepository;
import com.nate.bankingsystemapi.repository.TransactionRepository;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.service.ITransactionService;
import com.nate.bankingsystemapi.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class ConcurrentWithdrawalAndTransferTest {

    @Autowired
    private ITransactionService transactionService;

    @Autowired
    private UserRepository repoU;

    @Autowired
    private AccountRepository repo;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private LedgerEntryRepository lRepo;
    @Autowired
    private TransactionRepository tRepo;

    private User testUser;
    private Account testAcc1;
    private Account testAcc2;

    @BeforeEach
    void setUp() {
        lRepo.deleteAll();
        tRepo.deleteAll();
        repo.deleteAll();
        repoU.deleteAll();

        testUser = new User();
        testUser.setUsername("testUser");
        testUser.setRole(Role.USER);
        testUser.setFullName("Tester");
        testUser.setPassword(passwordEncoder.encode("password"));
        repoU.save(testUser);

        testAcc1 = new Account();
        testAcc1.setAccountNum(JwtUtil.generateAccNum());
        testAcc1.setBalance(1000L);
        testAcc1.setCurrency("Zar");
        testAcc1.setUser(testUser);
        repo.save(testAcc1);

        testAcc2 = new Account();
        testAcc2.setAccountNum(JwtUtil.generateAccNum());
        testAcc2.setBalance(500L);
        testAcc2.setCurrency("Zar");
        testAcc2.setUser(testUser);
        repo.save(testAcc2);
    }


    @RepeatedTest(10)
    void testConcurrentWithdrawal() throws InterruptedException {
        Long accountNum = testAcc1.getAccountNum();
        Long withdrawalAmount = 300L;
        int threadCount = 5;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();



        Runnable withdrawalTask = () -> {
                try {
                    startLatch.await();
                    String reqId = UUID.randomUUID().toString();

                    transactionService.withdrawFunds(new FundsRequest(accountNum,withdrawalAmount,reqId),"testUser");
                    successCount.incrementAndGet();
                }
                catch (Exception e){
                    System.out.println("Failed withdraw: " +e.getMessage() );
                    failureCount.incrementAndGet();
                }
                finally {
                    doneLatch.countDown();
                }

        };

        for(int i = 0; i<threadCount;i++) {
            executor.submit(withdrawalTask);
        }

        startLatch.countDown();
        doneLatch.await();

        Account updatedAcc = repo.findByAccountNum(accountNum).orElseThrow();

        int success = successCount.get();
        int failure = failureCount.get();

        assertEquals(threadCount, success+failure);
        assertTrue(success<=3);

        assertEquals(1000L - (success * withdrawalAmount),updatedAcc.getBalance());
        assertTrue(updatedAcc.getBalance()>=0);

        assertEquals(success, tRepo.count());
        assertEquals(success, lRepo.count());
    }


    @RepeatedTest(20)
    void testSimultaneousDepositAndWithdrawal() throws InterruptedException {
        Long accountNum = testAcc1.getAccountNum();
        Long withdrawalAmount = 500L;
        Long depositAmount = 100L;
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        CountDownLatch withdrawStartLatch = new CountDownLatch(1);
        CountDownLatch withdrawDoneLatch = new CountDownLatch(threadCount);

        CountDownLatch depositStartLatch = new CountDownLatch(1);
        CountDownLatch depositDoneLatch = new CountDownLatch(threadCount);

        AtomicInteger depositSuccessCount = new AtomicInteger();
        AtomicInteger withdrawSuccessCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        Runnable withdrawalTask = () -> {
            try{
                withdrawStartLatch.await();
                String reqId = UUID.randomUUID().toString();
                transactionService.withdrawFunds(new FundsRequest(accountNum,withdrawalAmount,reqId),"testUser");
                withdrawSuccessCount.incrementAndGet();
            } catch (Exception e) {
                System.out.println("Failed to withdraw: " +e.getMessage() );
                failureCount.incrementAndGet();
            }
            finally {
                withdrawDoneLatch.countDown();
            }
        };

        Runnable depositTask = () -> {
            try{
                depositStartLatch.await();
                String reqID = UUID.randomUUID().toString();
                transactionService.depositFunds(new FundsRequest(accountNum,depositAmount,reqID),"testUser");
                depositSuccessCount.incrementAndGet();
            } catch (Exception e) {
                System.out.println("Failed to Deposit: " + e.getMessage());
            }
            finally {
                depositDoneLatch.countDown();
            }
        };

        for(int i = 0; i<threadCount;i++){
            executor.submit(depositTask);
            executor.submit(withdrawalTask);
        }

        depositStartLatch.countDown();
        withdrawStartLatch.countDown();

        depositDoneLatch.await();
        withdrawDoneLatch.await();

        Account updatedAcc = repo.findByAccountNum(accountNum).orElseThrow();
        Long expectedBalance = 1000L + (depositSuccessCount.get()*depositAmount) - (withdrawSuccessCount.get() * withdrawalAmount);

        assertEquals(expectedBalance,updatedAcc.getBalance());

        assertTrue(updatedAcc.getBalance()>=0);

        assertEquals(depositSuccessCount.get() + withdrawSuccessCount.get(), tRepo.count());
        assertEquals(depositSuccessCount.get() + withdrawSuccessCount.get(), lRepo.count());



    }

    @RepeatedTest(10)
    void testConcurrentTransfer() throws InterruptedException {
        Long fromAcc = testAcc1.getAccountNum();
        Long toAcc = testAcc2.getAccountNum();
        int threadCount = 7;
        Long transferAmount = 200L;



        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();



        Runnable withdrawalTask = () -> {
            try {
                startLatch.await();
                String reqId = UUID.randomUUID().toString();

                transactionService.transfer(fromAcc, toAcc,transferAmount,"testUser",reqId);
                successCount.incrementAndGet();
            }
            catch (Exception e){
                System.out.println("Failed withdraw: " +e.getMessage() );
                failureCount.incrementAndGet();
            }
            finally {
                doneLatch.countDown();
            }

        };

        for(int i = 0; i<threadCount;i++) {
            executor.submit(withdrawalTask);
        }

        startLatch.countDown();

        doneLatch.await();

        Account from = repo.findByAccountNum(fromAcc).orElseThrow();
        Account to = repo.findByAccountNum(toAcc).orElseThrow();


        int success = successCount.get();

        int failure = failureCount.get();

        assertEquals(threadCount, success+failure);

        assertTrue(success<=5);

        assertEquals(1000L - (success * transferAmount),from.getBalance());
        assertEquals(500L  + (success * transferAmount),to.getBalance());

        assertEquals(1500L,from.getBalance() + to.getBalance());

        assertTrue(from.getBalance()>=0);

        assertEquals(successCount.get() *2L,lRepo.count());
        assertEquals(successCount.get() ,tRepo.count());
    }


    @RepeatedTest(10)
    void testIdempotencyWithdrawal() throws InterruptedException {
        Long fromAcc = testAcc1.getAccountNum();
        Long withdrawAmount  = 200L;
        int threadCount = 7;
        String reqId = UUID.randomUUID().toString();

        ExecutorService excecutor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        Runnable withdrawalTask = () -> {
            try{
                startLatch.await();
                transactionService.withdrawFunds(new FundsRequest(fromAcc,withdrawAmount,reqId),"testUser");
                successCount.incrementAndGet();
            }
            catch (Exception e){
                System.out.println("Failed withdraw: " +e.getMessage() );
                failureCount.incrementAndGet();
            }
            finally {
                doneLatch.countDown();
            }
        };

        for(int i = 0; i<threadCount;i++) {
            excecutor.submit(withdrawalTask);
        }

        startLatch.countDown();
        doneLatch.await();


        Account updatedAcc = repo.findByAccountNum(fromAcc).orElseThrow();

        assertEquals(threadCount - 1, failureCount.get());

        assertTrue(updatedAcc.getBalance()>=0);

        assertEquals(1, successCount.get());
        assertEquals(6,failureCount.get());

        assertEquals(1000 - (successCount.get() * withdrawAmount), updatedAcc.getBalance());
        assertEquals(successCount.get() ,tRepo.count());
        assertEquals(successCount.get() ,lRepo.count());
    }

    @RepeatedTest(10)
    void testIdempotencyDeposit() throws InterruptedException {
        Long fromAcc = testAcc1.getAccountNum();
        Long depositAmount  = 200L;
        int threadCount = 7;
        String reqId = UUID.randomUUID().toString();

        ExecutorService excecutor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();

        Runnable depositTask = () -> {
            try{
                startLatch.await();
                transactionService.depositFunds(new FundsRequest(fromAcc,depositAmount,reqId),"testUser");
                successCount.incrementAndGet();
            }
            catch (Exception e){

            }
            finally {
                doneLatch.countDown();
            }
        };

        for(int i = 0; i<threadCount;i++) {
            excecutor.submit(depositTask);
        }


        startLatch.countDown();
        doneLatch.await();


        Account updatedAcc = repo.findByAccountNum(fromAcc).orElseThrow();

        assertEquals(1, successCount.get());

        assertEquals(1000 + depositAmount, updatedAcc.getBalance());

        assertEquals(successCount.get() ,tRepo.count());
        assertEquals(successCount.get() ,lRepo.count());
    }

    @RepeatedTest(10)
    void testIdempotencyTransfer() throws InterruptedException {
        Long fromAcc = testAcc1.getAccountNum();
        Long toAcc = testAcc2.getAccountNum();
        Long transferAmount = 300L;
        String reqId = UUID.randomUUID().toString();
        int threadCount = 7;

        ExecutorService excecutor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        Runnable transferTask = () -> {
            try{
                startLatch.await();
                transactionService.transfer(fromAcc,toAcc,transferAmount,"testUser",reqId);
                successCount.incrementAndGet();
            }
            catch (Exception e){
                System.out.println("Failed transfer: " +e.getMessage() );
                failureCount.incrementAndGet();
            }
            finally {
                doneLatch.countDown();
            }
        };

        for(int i = 0; i<threadCount;i++) {
            excecutor.submit(transferTask);
        }


        startLatch.countDown();
        doneLatch.await();


        Account updatedFromAcc = repo.findByAccountNum(fromAcc).orElseThrow();
        Account updtaedToAcc = repo.findByAccountNum(toAcc).orElseThrow();

        assertEquals(1, successCount.get());
        assertEquals(6, failureCount.get());

        assertEquals(1000 - transferAmount, updatedFromAcc.getBalance());
        assertEquals(500 + transferAmount, updtaedToAcc.getBalance());

        assertEquals(successCount.get() ,tRepo.count());
        assertEquals(successCount.get() * 2L ,lRepo.count());




    }


}
