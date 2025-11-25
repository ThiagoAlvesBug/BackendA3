package com.ucdual.transaction_service.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // ID do usuário que fez a transação
    private Double amount; // valor

    @Enumerated(EnumType.STRING)
    private TransactionType type; // "DEBIT" ou "CREDIT"

    @Enumerated(EnumType.STRING)
    private TransactionStatus status = TransactionStatus.PENDING;

    private Long senderId;

    private String description;
    private LocalDateTime createdAt = LocalDateTime.now();
}
