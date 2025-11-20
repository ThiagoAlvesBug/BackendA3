package com.ucdual.transaction_service.dto;

import lombok.Data;

@Data
public class DepositRequest {
    private Long userId;
    private Double amount;
}
