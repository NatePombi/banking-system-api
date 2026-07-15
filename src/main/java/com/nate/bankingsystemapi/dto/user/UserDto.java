package com.nate.bankingsystemapi.dto.user;

import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.model.user.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class UserDto {
    private Long id;
    private String fullName;
    private String username;
    private Role role;
    private UserStatus status;
}
