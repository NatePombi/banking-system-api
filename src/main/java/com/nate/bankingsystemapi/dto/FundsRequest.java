package com.nate.bankingsystemapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Deposit or Withdraw Request")
@AllArgsConstructor
@Getter
@Setter
public class FundsRequest {
    @Schema(description = "Id of the account you wish to withdraw or deposit money to")
    @NotNull(message = "Id cannot be empty")
    private Long accountId;
    @Schema(description = "Amount you want to deposit or withdraw",example = "1000.00")
    @NotNull(message = "Amount cannot be empty")
    @Min(0)
    private Long amount;
}
