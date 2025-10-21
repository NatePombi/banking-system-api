package com.nate.bankingsystemapi.mapper;

import com.nate.bankingsystemapi.dto.UserDto;
import com.nate.bankingsystemapi.model.User;

public class UserMapper {

    public static UserDto toDto(User user){
        if(user == null){
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRoles()
        );
    }
}
