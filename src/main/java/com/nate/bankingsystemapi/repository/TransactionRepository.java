package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transactions,Long> {

//    Optional<Transactions> findByRequestIDAndUsername(
//            String reqId,
//            String username);
}
