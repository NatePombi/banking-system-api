package com.nate.bankingsystemapi.service.transaction;

import com.nate.bankingsystemapi.exception.TransactionNotFoundException;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class TransactionFailureService {

    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failTransaction(Long transactionId, String message) {

            Transactions transactions = transactionRepository.findById(transactionId).orElseThrow(() -> new TransactionNotFoundException("Transaction not found"));

            transactions.markFailed(message);
            transactionRepository.save(transactions);


    }




}
