package com.nate.bankingsystemapi.dto;

import com.nate.bankingsystemapi.model.User;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AccountDto {
    private Long id;
    private Long balanceCent;
    private String currency;
    private Long user;
    private Integer integer;
}
