package com.nate.bankingsystemapi.dto.transaction;

import com.nate.bankingsystemapi.model.transaction.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class TransactionDto {
    private Long id;
    private BigDecimal amount;
    private Long fromAccount;
    private Long toAccount;
    private Status status;
}
