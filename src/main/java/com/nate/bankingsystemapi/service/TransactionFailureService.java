package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.model.Status;
import com.nate.bankingsystemapi.model.Transactions;
import com.nate.bankingsystemapi.repository.TransactionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TransactionFailureService {
    private final TransactionRepository repo;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Transactions tx) {
        tx.changeStatus(Status.FAILED);
    }
}
