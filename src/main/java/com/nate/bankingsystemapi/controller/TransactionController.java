package com.nate.bankingsystemapi.controller;

import com.nate.bankingsystemapi.dto.FundsRequest;
import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.dto.TransferRequest;
import com.nate.bankingsystemapi.model.CustomerDetails;
import com.nate.bankingsystemapi.service.ITransactionService;
import com.nate.bankingsystemapi.util.RetryHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Transaction Controller",description = "End points for managing accounts")
@RestController
@RequestMapping("transaction")
@AllArgsConstructor
public class TransactionController {

    private final ITransactionService service;

    @Operation(summary = "Transferring funds")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Funds successfully transferred"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
            @ApiResponse(responseCode = "403",description = "Forbidden"),
            @ApiResponse(responseCode = "404",description = "User not found"),
            @ApiResponse(responseCode = "404", description = "Account Not found")
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> transfer(@RequestBody @Valid TransferRequest request, @AuthenticationPrincipal CustomerDetails details) throws Exception {
           return RetryHelper.retryOnLock(
                   ()-> ResponseEntity.ok().body(service.transfer(request,details.getUser().getId())),
                   3
           );


    }


    @Operation(summary = "Deposit funds")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Funds successfully deposited"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
            @ApiResponse(responseCode = "403",description = "Forbidden"),
            @ApiResponse(responseCode = "404",description = "User not found"),
            @ApiResponse(responseCode = "404", description = "Account Not found")
    })
    @PostMapping("/deposit")
    public ResponseEntity<String> depositFunds(@RequestBody @Valid FundsRequest req, @AuthenticationPrincipal CustomerDetails details) throws Exception {
           return RetryHelper.retryOnLock(
                   () -> ResponseEntity.ok(service.depositFunds(req,details.getUser().getId())),
                   3
           );

    }

    @Operation(summary = "Withdraw funds")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Funds successfully withdraw"),
            @ApiResponse(responseCode = "400",description = "Bad Request"),
            @ApiResponse(responseCode = "403",description = "Forbidden"),
            @ApiResponse(responseCode = "404",description = "User not found"),
            @ApiResponse(responseCode = "404", description = "Account Not found")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<String> withdrawFunds(@RequestBody @Valid FundsRequest req, @AuthenticationPrincipal CustomerDetails details) throws Exception {
            return RetryHelper.retryOnLock(
                    () -> ResponseEntity.ok().body(service.withdrawFunds(req, details.getUser().getId())),
                    3
            );
    }
}
