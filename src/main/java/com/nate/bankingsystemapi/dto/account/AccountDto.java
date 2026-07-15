package com.nate.bankingsystemapi.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@AllArgsConstructor
@Getter
public class AccountDto {
    private Long id;
    private Long accountNum;
    private BigDecimal balance;
    private String currency;
    private Long userId;
}
