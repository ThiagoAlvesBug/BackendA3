package com.ucdual.transaction_service.repository;

import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Transaction> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, TransactionStatus status);

}
