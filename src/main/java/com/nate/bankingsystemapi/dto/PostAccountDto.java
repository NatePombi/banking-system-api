package com.nate.bankingsystemapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Account Creation Request")
public class PostAccountDto {
    @NotNull
    @Min(0)
    @Schema(description = "Balance of the account",example = "1000.00")
    private Long balance;

}
