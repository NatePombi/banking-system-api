package com.nate.bankingsystemapi.service;

import com.nate.bankingsystemapi.dto.JwtResponse;
import com.nate.bankingsystemapi.dto.LoginDto;
import com.nate.bankingsystemapi.dto.RegisterDto;
import com.nate.bankingsystemapi.dto.UserDto;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
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
     */
    @Override
    public UserDto registerUser(RegisterDto registerDto) {
        log.info("Registers user: {}",registerDto.getUsername());

        //Creating User entity to store registered User
        User user = new User(registerDto.getFullName(),registerDto.getUsername(),encoder.encode(registerDto.getPassword()));

        //Saves registered user
        log.debug("Saves the registered user entity into repo");
        User saved = repo.save(user);

        //Maps User entity to UserDto object using mapper
        return UserMapper.toDto(saved);
    }

    @Override
    public JwtResponse loginUser(LoginDto loginDto) {
        log.info("Logging in user: {}",loginDto.getUsername());

        //Fetching user by username
        CustomerDetails customerDetails = (CustomerDetails) loadUserByUsername(loginDto.getUsername());

        if(!encoder.matches(loginDto.getPassword(), customerDetails.getPassword())){
            log.error("Password or Username invalid");
            throw new RuntimeException("Password or Username invalid");
        }


        String token = service.generateToken(customerDetails.getUser());

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
