package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account,Long> {


    Page<Account> findByUserUsername(String username, Pageable pageable);
}
