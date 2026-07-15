package com.nate.bankingsystemapi.service.user;

import com.nate.bankingsystemapi.dto.user.JwtResponse;
import com.nate.bankingsystemapi.dto.user.LoginDto;
import com.nate.bankingsystemapi.dto.user.RegisterDto;
import com.nate.bankingsystemapi.dto.user.UserDto;
import com.nate.bankingsystemapi.exception.InvalidCredentialException;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.exception.UsernameExistsException;
import com.nate.bankingsystemapi.mapper.UserMapper;
import com.nate.bankingsystemapi.model.user.entity.User;
import com.nate.bankingsystemapi.model.user.enums.Role;
import com.nate.bankingsystemapi.repository.UserRepository;
import com.nate.bankingsystemapi.security.JwtService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements IUserService {

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
     * @throws InvalidCredentialException if credentials are invalid
     */
    @Override
    public JwtResponse loginUser(LoginDto loginDto) {
        log.info("Logging in user: {}",loginDto.getUsername());

        //Fetching user by username
        User user =  repo.findByUsername(loginDto.getUsername())
                .orElseThrow(()->{
                    log.error("User not found: {}",loginDto.getUsername());
                    return new InvalidCredentialException("Password or Username invalid");
                });

        //checking if encoded passwords match, if not throws an exception
        if(!encoder.matches(loginDto.getPassword(), user.getPassword())){
            log.error("Password or Username invalid");
            throw new InvalidCredentialException("Password or Username invalid");
        }

        // Generates Jwt token for user login
        String token = service.generateToken(user);

        // returns object containing jwt token
        return new JwtResponse(token);
    }

    /**
     * Fetching users for admin
     *
     * @param username is username of the admin thats logged in
     * @param page the page number that user wants to receive. (0 based)
     * @param size the amount of items per page
     * @param sortBy the field the page is sorted by (e.g. id)
     * @param direction the way the pages are sorted. (ascending or descending)
     * @return a {@link Page} of {@link UserDto} object
     * @throws AccessDeniedException if admin was not found
     */
    @Override
    public Page<UserDto> adminGetAllUser(String username, int page, int size, String sortBy, String direction) {
        log.info("Attempting to get all Users for admin");

        if(!repo.existsByUsernameAndRole(username, Role.ADMIN)){
            log.warn("Admin not found. user: {}", username);
            throw new AccessDeniedException(String.format("Admin user %s not found",username));
        }

        Sort sort = direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending():Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page,size,sort);


        return repo.findAll(pageable).map(UserMapper::toDto);
    }


    /**
     * Admin Retrieving user
     *
     * @param id is id of user that admin wats to retreive
     * @param username is username of admin thats logged in
     * @return a {@link UserDto} object
     * @throws AccessDeniedException if admin is not found
     * @throws UserNotFoundException if user with id given was not found
     */
    @Override
    public UserDto adminGetUserById(Long id, String username) {
        log.warn("Admin attempting to get User by id: {}",id);

        if(!repo.existsByUsernameAndRole(username, Role.ADMIN)){
            log.warn("Admin not found. user: {}", username);
            throw new AccessDeniedException(String.format("Admin user %s not found",username));
        }

        User user = repo.findById(id).orElseThrow(()->{
            log.error("User not found. id: {}",id);
            return new UserNotFoundException();
        });

        return UserMapper.toDto(user);

    }


}
