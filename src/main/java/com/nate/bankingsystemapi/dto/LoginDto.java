package com.nate.bankingsystemapi.dto;

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
@Schema(description = "Login Request")
public class LoginDto {
    @Schema(description = "Username of user",example = "john")
    @NotBlank(message = "Username cannot be empty")
    private String username;
    @Schema(description = "Password of user",example = "john123")
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
