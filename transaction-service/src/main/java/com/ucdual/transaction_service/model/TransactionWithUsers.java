package com.ucdual.transaction_service.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class TransactionWithUsers {
    private Long id;

    private Long senderId;
    private Long receiverId;
    private Double amount;
    private TransactionStatus status;
    private String description;
    private LocalDateTime createdAt;
    private User sender;
    private User receiver;
}
