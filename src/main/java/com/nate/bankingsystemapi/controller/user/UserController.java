package com.nate.bankingsystemapi.controller.user;

import com.nate.bankingsystemapi.dto.user.LoginDto;
import com.nate.bankingsystemapi.dto.user.RegisterDto;
import com.nate.bankingsystemapi.dto.user.UserDto;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Controller",description = "End points for managing users")
@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/auth")
public class UserController {

    private final UserService service;

    @Operation(summary = "Registering User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered , returns User details"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody @Valid RegisterDto registerDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registerUser(registerDto));
    }


    @Operation(summary = "Logging in User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in, returns Jwt Token"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse( responseCode = "404" , description = "User Not Found")
    })
    @PostMapping("/login")
    public ResponseEntity<?> logUser(@RequestBody @Valid LoginDto dto){
        try{
            return ResponseEntity.ok().body(service.loginUser(dto));
        }
        catch (UserNotFoundException ex){
            return ResponseEntity.status(404).body(ex.getMessage());
        }
        catch (RuntimeException e){
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}
