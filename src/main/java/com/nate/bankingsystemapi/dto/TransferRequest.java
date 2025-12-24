package com.nate.bankingsystemapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private Long amount;
    @Schema(description = "Unique UUID to prevent duplicate requests")
    @NotNull(message = "Missing UUID")
    private String requestID;

}
