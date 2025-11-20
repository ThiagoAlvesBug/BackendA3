package com.ucdual.transaction_service.controller;

import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @GetMapping("/balance/{userId}")
    public Double getBalance(@PathVariable Long userId) {
        return service.getBalance(userId);
    }

    @PostMapping("/deposit")
    public String deposit(@RequestBody DepositRequest request) {
        return service.deposit(request);
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferRequest request) {
        return service.transfer(request);
    }

    @GetMapping("/{userId}")
    public List<Transaction> list(@PathVariable Long userId) {
        return service.listTransactions(userId);
    }
}
