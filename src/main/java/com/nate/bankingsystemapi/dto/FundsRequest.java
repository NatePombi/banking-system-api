package com.nate.bankingsystemapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Schema(description = "Deposit or Withdraw Request")
@Getter
@Setter
public class FundsRequest {
    @Schema(description = "Account number of the account you wish to withdraw or deposit money to")
    @NotNull(message = "Account number cannot be empty")
    private Long accountNum;
    @Schema(description = "Amount you want to deposit or withdraw",example = "1000.00")
    @NotNull(message = "Amount cannot be empty")
    @Min(0)
    private Long amount;
    @Schema(description = "Unique UUID to prevent duplicate requests")
    @NotNull(message = "Missing UUID")
    private String requestID;

    public FundsRequest(Long accountNum, Long amount, String requestID) {
        this.accountNum = accountNum;
        this.amount = amount;
        this.requestID = requestID;
    }

}
