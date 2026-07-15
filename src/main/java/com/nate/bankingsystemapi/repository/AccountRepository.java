package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account,Long> {


    Page<Account> findByUser(User user, Pageable pageable);


    boolean existsByUser(User user);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.accountNum = :accNum")
    Optional<Account> findByAccountNumForUpdate(@Param("accNum") Long accNum);

    @Query("""
            SELECT a FROM Account a
            WHERE a.accountNum = :accountNum
            """)
    Optional<Account> findByAccountNum(@Param("accountNum") Long accountNum);

    Optional<Account> findByIdAndUserId(Long id, Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :acc")
    Optional<Account> findByIdForUpdate(@Param("acc") Long acc);
}
