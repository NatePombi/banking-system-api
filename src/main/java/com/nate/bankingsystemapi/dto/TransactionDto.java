package com.nate.bankingsystemapi.dto;

import com.nate.bankingsystemapi.model.Account;
import com.nate.bankingsystemapi.model.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SecondaryRow;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TransactionDto {
    private Long id;
    private Long amount;
    private Long fromAccount;
    private Long toAccount;
    private Status status;
}
