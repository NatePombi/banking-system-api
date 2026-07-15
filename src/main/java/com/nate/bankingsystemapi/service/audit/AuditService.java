package com.nate.bankingsystemapi.service.audit;

import com.nate.bankingsystemapi.model.account.entity.Account;
import com.nate.bankingsystemapi.model.audit.enums.Action;
import com.nate.bankingsystemapi.model.audit.entity.AuditLog;
import com.nate.bankingsystemapi.model.transaction.entity.Transactions;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuditService implements IAuditService{

    private final AuditLogRepository auditLogRepository;

    @Override
    public void logTransfer(Transactions transactions, User user, Account from, Account to, BigDecimal amount) {
        String details = String.format(
                "Transaction %d: %s transferred %s %s from account $d to account %d",
                transactions.getId(),
                user.getUsername(),
                from.getCurrency(),
                amount,
                from.getAccountNum(),
                to.getAccountNum()
        );

        AuditLog auditLog = new AuditLog(Action.TRANSFER,user.getUsername(),details);

        auditLogRepository.save(auditLog);
    }
}
