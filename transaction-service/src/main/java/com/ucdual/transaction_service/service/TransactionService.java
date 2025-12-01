package com.ucdual.transaction_service.service;

import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.model.TransactionWithUsers;

import java.util.List;

public interface TransactionService {
    // Obtenção do saldo do usuário
    Double getBalance(Long userId);
    // Realização de depósito na conta do usuário
    //void deposit(DepositRequest request);
    // Realização de transferência para outro usuário
    void transfer(TransferRequest request);
    // Confirmação ou rejeição de transação pendente
    void confirmTransaction(Long userId, Long transactionId, boolean accepted);
    // Listagem de transações pendentes do usuário
    List<TransactionWithUsers> listPendingTransactions(Long userId);
    // Listagem de transações confirmadas do usuário
    List<TransactionWithUsers> listConfirmedTransactions(Long userId);
    // Listagem de todas as transações do usuário
    List<Transaction> listTransactions(Long userId);
}
