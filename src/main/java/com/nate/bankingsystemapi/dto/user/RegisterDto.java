package com.nate.bankingsystemapi.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Register User Request")
public class RegisterDto {
    @Schema(description = "Full Name of User",example = "John Doe")
    @NotBlank(message = "Full name cannot be empty")
    private String fullName;
    @Schema(description = "Username of User",example = "john123")
    @NotBlank(message = "Username cannot be empty")
    private String username;
    @Schema(description = "Email of User",example = "eail@gmail.com")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @Schema(description = "Password of User",example = "john123")
    @Size(min = 7, message = "Password should be at least 7 characters")
    private String password;
}
