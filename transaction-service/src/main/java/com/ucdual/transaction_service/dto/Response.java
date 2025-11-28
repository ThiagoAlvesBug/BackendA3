package com.ucdual.transaction_service.dto;

import lombok.Data;

@Data
public class Response {
    private boolean success;
    private String message;

    public Response() {} // já existe

    public Response(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}