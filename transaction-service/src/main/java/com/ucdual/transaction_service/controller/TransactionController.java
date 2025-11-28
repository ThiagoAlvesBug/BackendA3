package com.ucdual.transaction_service.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.Response;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.service.TransactionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService service;

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance(@PathVariable Long userId) {
        try {
            Double balance = service.getBalance(userId);
            return ResponseEntity.ok(balance);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<Response> deposit(@PathVariable Long userId, @RequestBody DepositRequest request) {
        try {
            request.setUserId(userId);
            service.deposit(request);
            return ResponseEntity.ok(new Response(true, "Depósito realizado com sucesso"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response(false, e.getMessage()));
        }
    }

    @PostMapping("/transfer")
    public ResponseEntity<Response> transfer(@PathVariable Long userId, @RequestBody TransferRequest request) {
        try {
            request.setUserId(userId);
            service.transfer(request);
            return ResponseEntity.ok(new Response(true, "Transferência criada com sucesso (pendente de confirmação)"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(false, e.getMessage()));
        }
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(@PathVariable Long userId) {
        try {
            List<Transaction> transacoes = service.listTransactions(userId);
            return ResponseEntity.ok(transacoes);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/transactions/pending")
    public ResponseEntity<List<Transaction>> listPending(@PathVariable Long userId) {
        try {
            List<Transaction> pending = service.listPendingTransactions(userId);
            return ResponseEntity.ok(pending);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/transactions/{transactionId}/confirm")
    public ResponseEntity<Response> confirm(
            @PathVariable Long userId,
            @PathVariable Long transactionId,
            @RequestParam boolean accepted) {

        try {
            service.confirmTransaction(userId, transactionId, accepted);
            return ResponseEntity.ok(new Response(true, "Transação atualizada com sucesso"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response(false, e.getMessage()));
        }
    }
}

