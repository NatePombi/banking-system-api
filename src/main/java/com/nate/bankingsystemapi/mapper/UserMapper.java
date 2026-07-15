package com.nate.bankingsystemapi.mapper;

import com.nate.bankingsystemapi.dto.user.UserDto;
import com.nate.bankingsystemapi.model.user.entity.User;

public class UserMapper {

    public static UserDto toDto(User user){
        if(user == null){
            return null;
        }

        return new UserDto(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getRole(),
                user.getStatus()
        );
    }
}
