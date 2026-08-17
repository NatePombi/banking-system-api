package com.nate.bankingsystemapi.service.transaction;

import com.nate.bankingsystemapi.dto.transaction.FundsRequest;
import com.nate.bankingsystemapi.dto.transaction.TransactionDto;
import com.nate.bankingsystemapi.dto.transaction.TransferRequest;
import com.nate.bankingsystemapi.model.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface ITransactionService {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    TransactionDto transfer(TransferRequest request, String authenticatedUserUsername, Long authenticatedUserId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    String depositFunds(FundsRequest req, String authenticatedUserUsername, Long authenticatedUserId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    String withdrawFunds(FundsRequest req, String authenticatedUserUsername, Long authenticatedUserId);


}
