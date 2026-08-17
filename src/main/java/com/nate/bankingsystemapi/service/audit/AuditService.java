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
    public void logTransfer(Long transactionId, String username, Account from, Account to, BigDecimal amount) {
        String details = String.format(
                "Transaction %d: %s transferred %s %s from account $d to account %d",
                transactionId,
                username,
                from.getCurrency(),
                amount,
                from.getAccountNum(),
                to.getAccountNum()
        );

        AuditLog auditLog = new AuditLog(Action.TRANSFER,username,details);

        auditLogRepository.save(auditLog);
    }

    @Override
    public void logDeposit(Long transactionId, String authenticatedUserUsername, Account account, BigDecimal amount) {
        String details = String.format(
                "Transaction %d: %s deposited %s %s into account: %d",
                transactionId,
                authenticatedUserUsername,
                account.getCurrency(),
                amount,
                account.getAccountNum()
        );

        AuditLog auditLog = new AuditLog(Action.DEPOSIT,authenticatedUserUsername,details);

        auditLogRepository.save(auditLog);
    }

    @Override
    public void logWithdraw(Long transactionId, String authenticatedUserUsername, Account account, BigDecimal amount) {
        String details = String.format(
                "Transaction %d: %s withdrew %s %s into account: %d",
                transactionId,
                authenticatedUserUsername,
                account.getCurrency(),
                amount,
                account.getAccountNum()
        );

        AuditLog auditLog = new AuditLog(Action.WITHDRAW,authenticatedUserUsername,details);

        auditLogRepository.save(auditLog);
    }


}
