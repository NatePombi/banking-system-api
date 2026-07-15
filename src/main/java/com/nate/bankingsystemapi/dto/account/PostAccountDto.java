package com.nate.bankingsystemapi.dto.account;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Currency cannot be empty")
    @Schema(description = "Currency of the account",example = "ZAR")
    private String currency;

}
