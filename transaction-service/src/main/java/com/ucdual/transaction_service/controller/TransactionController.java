package com.ucdual.transaction_service.controller;

import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.Response;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users/{userId}")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @GetMapping("/balance/")
    public Double getBalance(@PathVariable Long userId) {
        return service.getBalance(userId);
    }

    @PostMapping("/deposit")
    public String deposit(@RequestBody DepositRequest request) {
        return service.deposit(request);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@PathVariable Long userId, @RequestBody TransferRequest request) {
        try {
            request.setUserId(userId);
            service.transfer(request);
            Response response = new Response();
            response.setSuccess(true);
            response.setMessage("Transferência realizada com sucesso");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Response response = new Response();
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(response);
        }
    }

    @GetMapping("/transactions")
    public List<Transaction> list(@PathVariable Long userId) {
        return service.listTransactions(userId);
    }

}
