package com.ucdual.transaction_service.service;

import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Transaction;

import java.util.List;

public interface TransactionService {

    Double getBalance(Long userId);

    String deposit(DepositRequest request);

    void transfer(TransferRequest request);

    List<Transaction> listTransactions(Long userId);
}
