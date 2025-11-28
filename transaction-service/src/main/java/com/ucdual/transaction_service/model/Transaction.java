package com.ucdual.transaction_service.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Transaction {
    @Id
    @GeneratedValue
    private Long id;

    private Long senderId;
    private Long receiverId;
    private Double amount;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
}
