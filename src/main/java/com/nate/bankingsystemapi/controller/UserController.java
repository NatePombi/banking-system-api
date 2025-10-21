package com.nate.bankingsystemapi.controller;

import com.nate.bankingsystemapi.dto.JwtResponse;
import com.nate.bankingsystemapi.dto.LoginDto;
import com.nate.bankingsystemapi.dto.RegisterDto;
import com.nate.bankingsystemapi.exception.UserNotFoundException;
import com.nate.bankingsystemapi.service.UserService;
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

@RestController
@AllArgsConstructor
@RequestMapping("auth")
public class UserController {

    private final UserService service;


    @PostMapping("register")
    public ResponseEntity<String> register(@RequestBody @Valid RegisterDto registerDto){
        service.registerUser(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("Successfully Registered");
    }

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
