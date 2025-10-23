package com.nate.bankingsystemapi.controller;

import com.nate.bankingsystemapi.dto.TransactionDto;
import com.nate.bankingsystemapi.dto.TransferRequest;
import com.nate.bankingsystemapi.model.CustomerDetails;
import com.nate.bankingsystemapi.model.Transaction;
import com.nate.bankingsystemapi.service.ITransactionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("transaction")
@AllArgsConstructor
public class TransactionController {

    private final ITransactionService service;


    @PostMapping("/transfer")
    public ResponseEntity<TransactionDto> transfer(@RequestBody @Valid TransferRequest request, @AuthenticationPrincipal CustomerDetails details){
        return ResponseEntity.ok().body(service.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount(), details.getUsername() ));
    }
}
