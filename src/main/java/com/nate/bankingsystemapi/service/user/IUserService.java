package com.nate.bankingsystemapi.service.user;

import com.nate.bankingsystemapi.dto.user.JwtResponse;
import com.nate.bankingsystemapi.dto.user.LoginDto;
import com.nate.bankingsystemapi.dto.user.RegisterDto;
import com.nate.bankingsystemapi.dto.user.UserDto;
import org.springframework.data.domain.Page;

public interface IUserService {
    UserDto registerUser(RegisterDto registerDto);
    JwtResponse loginUser(LoginDto loginDto);
    Page<UserDto> adminGetAllUser(String email,int page, int size, String sortBy, String direction);
    UserDto adminGetUserById(Long id,String username);
}
