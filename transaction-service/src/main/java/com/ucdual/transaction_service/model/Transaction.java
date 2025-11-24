package com.ucdual.transaction_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;       // ID do usuário que fez a transação
    private Double amount;     // valor

    @Enumerated(EnumType.STRING)
    private TransactionType type; // "DEBIT" ou "CREDIT"
    
    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();
}
