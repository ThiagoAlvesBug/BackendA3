package com.ucdual.transaction_service.service;

import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Account;
import com.ucdual.transaction_service.model.TransactionStatus;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.model.TransactionType;
import com.ucdual.transaction_service.repository.AccountRepository;
import com.ucdual.transaction_service.repository.TransactionRepository;
import com.ucdual.transaction_service.repository.UserRepository;
import com.ucdual.transaction_service.model.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final UserRepository userRepo;
    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;

    // SALDO = soma das transações aprovadas
    @Override
    public Double getBalance(Long userId) {
        List<Transaction> userTransactions = transactionRepo.findAll().stream()
            .filter(tx -> tx.getUserId().equals(userId))
            .filter(tx -> tx.getStatus() == TransactionStatus.APPROVED)
            .toList();

        return userTransactions.stream()
            .mapToDouble(tx -> tx.getType() == TransactionType.CREDIT ? tx.getAmount() : -tx.getAmount())
            .sum();
    }   

    @Override
    @Transactional
    public String deposit(DepositRequest request) {
        Long userId = request.getUserId();
        Double amount = request.getAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Valor do depósito deve ser maior que zero.");
        }

        // transação de crédito aprovada
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType(TransactionType.CREDIT);
        tx.setStatus(TransactionStatus.APPROVED);
        tx.setDescription("Depósito");
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(tx);

        // atualiza saldo na tabela account
        accountRepo.findById(userId).ifPresent(account -> {
            Double current = account.getBalance() != null ? account.getBalance() : 0.0;
            account.setBalance(current + amount);
            accountRepo.save(account);
        });

        return "Depósito realizado com sucesso";
    }

    // TRANSFERÊNCIA (cria transação pendente)
    @Override
    @Transactional
    public void transfer(TransferRequest request) {

        Long senderId = request.getUserId();
        String targetPixKey = request.getPixKey();
        Double amount = request.getAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser maior que zero.");
        }

        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário de origem não encontrado."));

        User receiver = userRepo.findAll().stream()
                .filter(user -> user.getEmail().equals(targetPixKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chave Pix de destino não encontrada."));

        // valida saldo real
        Double saldo = getBalance(senderId);
        if (saldo < amount) {
            throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        }

        // cria transação pendente para o recebedor confirmar
        Transaction tx = new Transaction();
        tx.setSenderId(senderId);
        tx.setUserId(receiver.getId());
        tx.setAmount(amount);
        tx.setType(TransactionType.CREDIT);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setDescription("Pix pendente de " + sender.getName());
        tx.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(tx);
    }

    @Override
    public List<Transaction> listTransactions(Long userId) {
        return transactionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // CONFIRMAR PIX (AGORA CORRETO)
    @Override
    @Transactional
    public void confirmTransaction(Long id, boolean accepted) {

        Transaction tx = transactionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada"));

        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transação já foi analisada");
        }

        if (!accepted) {
            tx.setStatus(TransactionStatus.REJECTED);
            transactionRepo.save(tx);
            return;
        }

        // APROVAÇÃO -----------------------
        tx.setStatus(TransactionStatus.APPROVED);
        transactionRepo.save(tx);

        Long senderId = tx.getSenderId();
        Long receiverId = tx.getUserId();
        Double amount = tx.getAmount();

        // 3) Atualiza tabela Account
        accountRepo.findById(receiverId).ifPresent(acc -> {
            acc.setBalance(acc.getBalance() + amount);
            accountRepo.save(acc);
        });

        accountRepo.findById(senderId).ifPresent(acc -> {
            acc.setBalance(acc.getBalance() - amount);
            accountRepo.save(acc);
        });
    }

    @Override
    public List<Transaction> listPendingTransactions(Long userId) {
        return transactionRepo.findByUserIdAndStatusOrderByCreatedAtDesc(
                userId,
                TransactionStatus.PENDING);
    }

}
