package com.nate.bankingsystemapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class TransferRequest {
    @NotNull(message = "From Account cannot be empty")
    private Long fromAccount;
    @NotNull(message = "To Account cannot be empty")
    private Long toAccount;
    @Min(1)
    private Long amount;
}
