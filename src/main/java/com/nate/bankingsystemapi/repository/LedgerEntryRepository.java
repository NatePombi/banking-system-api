package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry,Long> {
}
