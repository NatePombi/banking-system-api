package com.nate.bankingsystemapi.controller;

import com.nate.bankingsystemapi.dto.JwtResponse;
import com.nate.bankingsystemapi.dto.LoginDto;
import com.nate.bankingsystemapi.dto.RegisterDto;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Controller",description = "End points for managing users")
@RestController
@AllArgsConstructor
@RequestMapping("auth")
public class UserController {

    private final UserService service;

    @Operation(summary = "Registering User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered , returns success message"),
            @ApiResponse(responseCode = "400", description = "Bad Request")
    })

    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterDto registerDto){
        service.registerUser(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully Registered");
    }


    @Operation(summary = "Logging in User")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in, returns Jwt Token"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse( responseCode = "404" , description = "User Not Found")
    })
    @PostMapping("login")
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
