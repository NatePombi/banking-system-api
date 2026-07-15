package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.user.JwtResponse;
import com.nate.bankingsystemapi.dto.user.LoginDto;
import com.nate.bankingsystemapi.dto.user.RegisterDto;
import com.nate.bankingsystemapi.dto.user.UserDto;
import com.nate.bankingsystemapi.exception.InvalidCredentialException;
import com.nate.bankingsystemapi.exception.UsernameExistsException;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.model.user.enums.UserStatus;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.security.JwtService;
import com.nate.bankingsystemapi.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

        verify(repo).save(any(User.class));
        verify(repo).existsByUsername(reg.getUsername());
    }

    @Test
    void testRegisterUser_Fail_DuplicateUsername(){
        RegisterDto registerDto = new RegisterDto("Tester","test","test@gmail.com","test123");
        when(repo.existsByUsername(registerDto.getUsername())).thenReturn(true);

        assertThrows(UsernameExistsException.class, ()->{
            service.registerUser(registerDto);
        });

        verify(repo,never()).save(any(User.class));
    }

    @Test
    void testLoginUser_Success(){
        LoginDto login = new LoginDto("test","test123");
        when(repo.findByUsername("test")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("Valid-Token");

        when(encoder.matches("test123",testUser.getPassword())).thenReturn(true);

        JwtResponse response = service.loginUser(login);

        assertEquals("Valid-Token", response.getToken());
        verify(repo).findByUsername("test");
        verify(encoder).matches("test123",testUser.getPassword());
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void shouldFailLogin_InvalidCredentials_Username(){
        LoginDto login = new LoginDto("test","test123");


        assertThrows(InvalidCredentialException.class,()->{
            service.loginUser(login);
        });
    }

    @Test
    void shouldFailLogin_InvalidCredentials_Password(){
        LoginDto login = new LoginDto("test","test123");
        when(repo.findByUsername(login.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(InvalidCredentialException.class,()->{
            service.loginUser(login);
        });
    }


    @Test
    void shouldGetAllUserForAdmin(){
        User user1 = mock(User.class);
        User user2 = mock(User.class);

        Pageable pageable = PageRequest.of(0,5, Sort.by("id").descending());
        Page<User> page = new PageImpl<>(List.of(user1,user2));

        when(repo.existsByUsernameAndRole("test", Role.ADMIN)).thenReturn(true);
        when(repo.findAll(pageable)).thenReturn(page);

        Page<UserDto> responses = service.adminGetAllUser("test",0,5,"id","desc");

        assertNotNull(responses);
        assertEquals(2,responses.getContent().size());

    }

    @Test
    void shouldFailGetAllUserForAdmin_AdminNotFound(){

        assertThrows(AccessDeniedException.class,()->{
            service.adminGetAllUser("test",0,5,"id","desc");
        });
    }

    @Test
    void shouldGetUserByIdForAdmin(){

        when(repo.existsByUsernameAndRole("admin",Role.ADMIN)).thenReturn(true);
        when(repo.findById(1L)).thenReturn(Optional.of(testUser));

        UserDto dto = service.adminGetUserById(1L,"admin");
        assertNotNull(dto);

        assertEquals("Tester",dto.getFullName());
        assertEquals("test",dto.getUsername());
        assertEquals(Role.USER,dto.getRole());
        assertEquals(UserStatus.ACTIVE,dto.getStatus());
    }

}
