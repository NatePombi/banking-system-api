package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.dto.TransferRequest;
import com.nate.bankingsystemapi.model.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

public interface ITransactionService {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    TransactionDto transfer(TransferRequest request,User user);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    String depositFunds(FundsRequest req, User user);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    String withdrawFunds(FundsRequest req, User user);


}
