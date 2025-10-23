package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
}
