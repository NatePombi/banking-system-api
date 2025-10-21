package com.nate.bankingsystemapi.dto;

import com.nate.bankingsystemapi.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDto {
    private Long id;
    private String username;
    private String password;
    private Role role;
}
