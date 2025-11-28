package com.ucdual.auth_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    private Long userId; // mesmo ID do usuário
    private Double balance = 0.0; // saldo inicial
}