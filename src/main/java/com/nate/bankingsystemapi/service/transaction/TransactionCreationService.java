package com.nate.bankingsystemapi.service.transaction;

import com.nate.bankingsystemapi.exception.DuplicateRequestException;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionCreationService {
    private final TransactionRepository transactionRepository;
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transactions createTransaction(String key){
        Transactions transactions = Transactions.create(key);

        try {
            return transactionRepository.save(transactions);
        }
        catch (DataIntegrityViolationException e){
            throw new DuplicateRequestException("Request already processed");
        }
    }
}
