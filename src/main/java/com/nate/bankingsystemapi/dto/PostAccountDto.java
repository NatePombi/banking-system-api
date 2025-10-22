package com.nate.bankingsystemapi.dto;

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
public class PostAccountDto {
    @NotNull
    @Min(0)
    private Long balanceCent;
    @NotBlank(message = "Currency cannot be empty")
    private String currency;
}
