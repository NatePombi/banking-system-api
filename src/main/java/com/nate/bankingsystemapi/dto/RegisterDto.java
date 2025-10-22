package com.nate.bankingsystemapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class RegisterDto {
    @NotBlank(message = "Full name cannot be empty")
    private String fullName;
    @NotBlank(message = "Username cannot be empty")
    private String username;
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @NotBlank(message = "Password cannot be empty")
    private String password;
}
