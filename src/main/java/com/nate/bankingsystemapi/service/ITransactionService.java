package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.dto.TransferRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface ITransactionService {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    TransactionDto transfer(TransferRequest request,Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    String depositFunds(FundsRequest req, Long userId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    String withdrawFunds(FundsRequest req, Long userId);


}
