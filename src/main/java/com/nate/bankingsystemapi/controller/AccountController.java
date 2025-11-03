package com.nate.bankingsystemapi.controller;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.dto.PaginatedResponse;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import com.nate.bankingsystemapi.model.CustomerDetails;
import com.nate.bankingsystemapi.service.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jdk.jfr.Percentage;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Account Controller",description = "End point for managing accounts")
@RestController
@RequestMapping("account")
@AllArgsConstructor
public class AccountController {

    private final IAccountService service;

    @Operation(summary = "Creates a new Account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",description = "account created, returns Account"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
            @ApiResponse(responseCode = "403",description = "Forbidden")
    })
    @PostMapping
    public ResponseEntity<AccountDto> create(@RequestBody @Valid PostAccountDto dto, @AuthenticationPrincipal CustomerDetails details){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(dto,details.getUsername()));
    }


    @Operation(summary = "Finding Account by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "successfully found account, returns account"),
            @ApiResponse(responseCode = "400", description = "Bad Request"),
            @ApiResponse(responseCode = "403",description = "Forbidden"),
            @ApiResponse(responseCode = "404",description = "Account not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getById(@PathVariable Long id, @AuthenticationPrincipal CustomerDetails details){
        return ResponseEntity.ok().body(service.getAccountById(id,details.getUsername()));
    }

    @Operation(summary = "Fetching a Paginated List of User Accounts")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "successfully returned list of user accounts"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @GetMapping
    public ResponseEntity<PaginatedResponse<AccountDto>> getAllUserAccounts(@AuthenticationPrincipal CustomerDetails details,
                                                                            @RequestParam(defaultValue = "0") int page,
                                                                            @RequestParam(defaultValue = "5") int size,
                                                                            @RequestParam(defaultValue = "id") String sortBy,
                                                                            @RequestParam(defaultValue = "desc") String direction){
        Page<AccountDto> accountDtoPage = service.getAllUserAccount(details.getUsername(),page,size,sortBy,direction);

        PaginatedResponse<AccountDto> response = new PaginatedResponse<>(
                accountDtoPage.getContent(),
                accountDtoPage.getNumber(),
                accountDtoPage.getTotalPages(),
                accountDtoPage.getTotalElements(),
                accountDtoPage.isLast()
        );

        return ResponseEntity.ok().body(response);
    }
}
