package com.nate.bankingsystemapi.util;

import jakarta.persistence.OptimisticLockException;
import org.springframework.dao.CannotAcquireLockException;

import java.util.concurrent.Callable;

public class RetryHelper {

    public static <T> T retryOnLock(Callable<T> callable, int maxAttempts) throws Exception {
        int attempts = 0;
        while (true) {
            try {
                return callable.call();
            } catch (CannotAcquireLockException | OptimisticLockException e) {
                attempts++;

                if (attempts > maxAttempts) {
                    throw new RuntimeException("Operation failed after retries", e);
                }

                Thread.sleep(5L * attempts);
            }
        }
    }
}
