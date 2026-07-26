package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.transaction.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transactions,Long> {


    int countByStatus(Status status);

    Optional<Transactions> findById(Long id);
}
