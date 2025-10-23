package com.nate.bankingsystemapi.repository;

import com.nate.bankingsystemapi.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog,Long> {

}
