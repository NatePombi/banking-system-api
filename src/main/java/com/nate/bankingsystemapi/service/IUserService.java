package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.JwtResponse;
import com.nate.bankingsystemapi.dto.LoginDto;
import com.nate.bankingsystemapi.dto.RegisterDto;
import com.nate.bankingsystemapi.dto.UserDto;

public interface IUserService {
    UserDto registerUser(RegisterDto registerDto);
    JwtResponse loginUser(LoginDto loginDto);
}
