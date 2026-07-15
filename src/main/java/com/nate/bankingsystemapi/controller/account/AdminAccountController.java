package com.nate.bankingsystemapi.controller.account;

import com.nate.bankingsystemapi.dto.account.AccountDto;
import com.nate.bankingsystemapi.dto.PaginatedResponse;
import com.nate.bankingsystemapi.service.account.IAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Account Controller", description = "Manages admin account requests")
@RestController
@RequestMapping("/api/v1/admin/accounts")
@RequiredArgsConstructor
public class AdminAccountController {
    private final IAccountService accountService;

    @Operation(summary = "Fetching a Paginated List of all Accounts for admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "successfully returned list of all accounts"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
    })
    @GetMapping("/fetch")
    public ResponseEntity<PaginatedResponse<AccountDto>> getAllAccounts(@AuthenticationPrincipal(expression = "username") String username,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "5") int size,
                                                                        @RequestParam(defaultValue = "id") String sortBy,
                                                                        @RequestParam(defaultValue = "desc") String direction) {
        Page<AccountDto> response = accountService.adminGetAllUserAccount(username,page,size,sortBy,direction);

        PaginatedResponse<AccountDto> paginatedResponse = new PaginatedResponse<>(
                response.getContent(),
                size,
                response.getTotalPages(),
                response.getTotalElements(),
                response.hasNext()
        );

        return ResponseEntity.ok(paginatedResponse);

    }

    @Operation(summary = "Fetching account by ID for admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "successfully retrieved account by ID"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
            @ApiResponse(responseCode = "404",description = "Account not found"),

    })
    @GetMapping("/fetch/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable long id, @AuthenticationPrincipal(expression = "username") String username) {
        return ResponseEntity.ok(accountService.adminGetAccountById(id, username));
    }

    @Operation(summary = "Fetching account by account number for admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "successfully retrieved an account by account number"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
            @ApiResponse(responseCode = "404",description = "Account not found"),
    })
    @GetMapping("fetch/accNum/{acc}")
    public ResponseEntity<AccountDto> getAccountByAccNum(@PathVariable long acc, @AuthenticationPrincipal(expression = "username") String username) {
        return ResponseEntity.ok(accountService.adminGetAccountByAccountNumber(acc, username));
    }
}
