package com.ucdual.transaction_service.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.Response;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.service.TransactionService;

import lombok.RequiredArgsConstructor;

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

    @PutMapping("/transactions/{transactionId}/confirm")
    public ResponseEntity<?> confirm(
            @PathVariable Long userId,
            @PathVariable Long transactionId,
            @RequestParam boolean accepted) {

        service.confirmTransaction(transactionId, accepted);
        return ResponseEntity.ok("Transação atualizada");
    }

    @GetMapping("/transactions/pending")
    public List<Transaction> listPending(@PathVariable Long userId) {
        return service.listPendingTransactions(userId);
    }

}
