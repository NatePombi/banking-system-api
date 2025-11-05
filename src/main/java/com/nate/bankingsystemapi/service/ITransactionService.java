package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;

public interface ITransactionService {
    TransactionDto transfer(Long fromId, Long toId, Long amountCents,String username);
    String depositFunds(FundsRequest req, String username);
    String withdrawFunds(FundsRequest req, String username);


}
