package com.ucdual.transaction_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Account {

    @Id
    private Long userId;   // mesmo ID do auth-service

    private Double balance = 0.0;
}
