package com.nate.bankingsystemapi.controller;

import com.nate.bankingsystemapi.dto.AccountDto;
import com.nate.bankingsystemapi.dto.PaginatedResponse;
import com.nate.bankingsystemapi.dto.PostAccountDto;
import com.nate.bankingsystemapi.model.CustomerDetails;
import com.nate.bankingsystemapi.service.IAccountService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("account")
@AllArgsConstructor
public class AccountController {

    private final IAccountService service;

    @PostMapping
    public ResponseEntity<AccountDto> create(@RequestBody @Valid PostAccountDto dto, @AuthenticationPrincipal CustomerDetails details){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createAccount(dto,details.getUsername()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDto> getById(@PathVariable Long id, @AuthenticationPrincipal CustomerDetails details){
        return ResponseEntity.ok().body(service.getAccountById(id,details.getUsername()));
    }

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
