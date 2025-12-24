package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface ITransactionService {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    TransactionDto transfer(Long fromId, Long toId, Long amountCents,String username,String reqId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    String depositFunds(FundsRequest req, String username);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    String withdrawFunds(FundsRequest req, String username);


}
