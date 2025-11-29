package com.ucdual.auth_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    private Long userId;
    private Double balance = 0.0;
}