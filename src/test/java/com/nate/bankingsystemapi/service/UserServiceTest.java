package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.JwtResponse;
import com.nate.bankingsystemapi.dto.LoginDto;
import com.nate.bankingsystemapi.dto.RegisterDto;
import com.nate.bankingsystemapi.dto.UserDto;
import com.nate.bankingsystemapi.exception.UsernameExistsException;
import com.nate.bankingsystemapi.model.User;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository repo;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private UserService service;


    private User testUser;

    @BeforeEach
    void startUp(){
        testUser = User.createUser("Tester","test",encoder.encode("test123"));
    }


    @Test
    void testRegisterUser_Success(){
        RegisterDto reg = new RegisterDto("Tester","test","test@gmail.com","test123");
        when(repo.save(any(User.class))).thenReturn(testUser);
        when(repo.existsByUsername(reg.getUsername())).thenReturn(false);


        UserDto user = service.registerUser(reg);

        assertEquals("Tester",user.getFullName(),"full name should be the same");
        assertEquals("test",user.getUsername(),"username should be the same");
    }

    @Test
    void testRegisterUser_Fail_DuplicateUsername(){
        RegisterDto registerDto = new RegisterDto("Tester","test","test@gmail.com","test123");
        when(repo.existsByUsername(registerDto.getUsername())).thenReturn(true);

        assertThrows(UsernameExistsException.class, ()->{
            service.registerUser(registerDto);
        });
    }

    @Test
    void testLoginUser_Success(){
        LoginDto login = new LoginDto("test","test123");

        when(repo.findByUsername("test")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("Valid-Token");
        UserDetails detail = service.loadUserByUsername(login.getUsername());

        when(encoder.matches(login.getPassword(),detail.getPassword())).thenReturn(true);

        JwtResponse response = service.loginUser(login);

        assertEquals(JwtResponse.class, response.getClass());
    }



}
