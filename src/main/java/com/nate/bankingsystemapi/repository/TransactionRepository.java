package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transactions,Long> {
//   @Query("""
//        select t from Transaction t
//        join t.fromAccount a
//        join a.user u
//        where t.requestID = :requestID
//        and u.username = :username
//""")
    Optional<Transactions> findByRequestIDAndUsername(
            String reqId,
            String username);
}
