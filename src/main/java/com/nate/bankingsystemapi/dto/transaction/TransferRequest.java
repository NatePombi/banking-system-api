package com.nate.bankingsystemapi.dto.transaction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@NoArgsConstructor
@Setter
@Getter
@Schema(description = "Transfer Request")
public class TransferRequest {
    @Schema(description = "Account number of the Account that's transferring")
    @NotNull(message = "From Account cannot be empty")
    private Long fromAccount;
    @Schema(description = "Account number of the Accounts that receiving the transfer")
    @NotNull(message = "To Account cannot be empty")
    private Long toAccount;
    @Schema(description = "The amount that's being transferred")
    @Min(1)
    private BigDecimal amount;
    @Schema(description = "Unique UUID to prevent duplicate requests")
    @NotNull(message = "Missing UUID")
    private String requestID;

    public TransferRequest(Long fromAccount, Long toAccount, BigDecimal amount, String requestID) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.amount = amount;
        this.requestID = requestID;
    }

}
