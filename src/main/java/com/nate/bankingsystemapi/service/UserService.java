package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.JwtResponse;
import com.nate.bankingsystemapi.dto.LoginDto;
import com.nate.bankingsystemapi.dto.RegisterDto;
import com.nate.bankingsystemapi.dto.UserDto;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.exception.UsernameExistsException;
import com.nate.bankingsystemapi.mapper.UserMapper;
import com.nate.bankingsystemapi.model.CustomerDetails;
import com.nate.bankingsystemapi.model.User;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.security.JwtService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements IUserService, UserDetailsService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final JwtService service;


    /**
     * Register User
     *
     * @param registerDto the {@link RegisterDto} object with user details for registration
     * @return a {@link UserDto} object
     * @throws UsernameExistsException if duplicate username is found
     */
    @Override
    public UserDto registerUser(RegisterDto registerDto) {
        log.info("Registering user");

        if(repo.existsByUsername(registerDto.getUsername())){
            log.warn("Duplicate username found Registration failed");
            throw new UsernameExistsException("User with Username already exists");
        }

        //Creating User entity to store registered User
        User user =  User.createUser(registerDto.getFullName(),registerDto.getUsername(),encoder.encode(registerDto.getPassword()));

        //Saves registered user
        User saved = repo.save(user);
        log.info("Saves the registered user entity into repo");

        //Maps User entity to UserDto object using mapper
        return UserMapper.toDto(saved);
    }

    /**
     * Logging in User
     *
     * @param loginDto the {@link LoginDto} object with user details for login
     * @return a {@link JwtResponse} object containing the JWT token
     */
    @Override
    public JwtResponse loginUser(LoginDto loginDto) {
        log.info("Logging in user: {}",loginDto.getUsername());

        //Fetching user by username
        CustomerDetails customerDetails = (CustomerDetails) loadUserByUsername(loginDto.getUsername());

        //checking if encoded passwords match, if not throws an exception
        if(!encoder.matches(loginDto.getPassword(), customerDetails.getPassword())){
            log.error("Password or Username invalid");
            throw new RuntimeException("Password or Username invalid");
        }

        // Generates Jwt token for user login
        String token = service.generateToken(customerDetails.getUser());

        // returns object containing jwt token
        return new JwtResponse(token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Fetching user by username: {}",username);

        //Fetches User by specified username, throws exception if not found
        User user = repo.findByUsername(username)
                .orElseThrow(()->{
                   log.error("User not found: {}",username);
                   return new UserNotFoundException();
                });

        // returns new CustomerDetails
        return new CustomerDetails(user);
    }
}
