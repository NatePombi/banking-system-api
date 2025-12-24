package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction,Long> {
//   @Query("""
//        select t from Transaction t
//        join t.fromAccount a
//        join a.user u
//        where t.requestID = :requestID
//        and u.username = :username
//""")
    Optional<Transaction> findByRequestIDAndUsername(
            String reqId,
            String username);
}
