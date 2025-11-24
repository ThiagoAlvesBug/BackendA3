package com.ucdual.transaction_service.dto;

import lombok.Data;

@Data
public class TransferRequest {
    private Long userId;
    private String pixKey;
    private Double amount;
    private String description;
}
