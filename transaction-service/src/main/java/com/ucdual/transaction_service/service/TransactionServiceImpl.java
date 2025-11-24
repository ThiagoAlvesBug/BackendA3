package com.ucdual.transaction_service.service;

import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Account;
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

    @Override
    public Double getBalance(Long userId) {
        List<Transaction> userTransactions = transactionRepo.findAll().stream()
                .filter(tx -> tx.getUserId().equals(userId))
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

        // registra a transação de crédito
        Transaction tx = new Transaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType(TransactionType.CREDIT);
        tx.setDescription("Depósito");
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepo.save(tx);

        // opcional: atualizar tabela de Account, se você estiver usando esse saldo
        accountRepo.findById(userId).ifPresent(account -> {
            Double current = account.getBalance() != null ? account.getBalance() : 0.0;
            account.setBalance(current + amount);
            accountRepo.save(account);
        });

        return "Depósito realizado com sucesso";
    }

    @Override
    @Transactional
    public void transfer(TransferRequest request) {

        Long sourceUserId = request.getUserId();
        String targetPixKey = request.getPixKey();
        Double amount = request.getAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser maior que zero.");
        }

        // 1. valida saldo da origem usando Account.balance
        Double saldoOrigem = getBalance(sourceUserId);
        if (saldoOrigem < amount) {
            throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        }

        User sourceUser = userRepo.findById(sourceUserId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário de origem não encontrado."));

        User targetUser = userRepo.findAll().stream()
                .filter(user -> user.getEmail().equals(targetPixKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chave Pix de destino não encontrada."));

        // 2. cria transação de DÉBITO (origem)
        Transaction debito = new Transaction();
        debito.setUserId(sourceUser.getId());
        debito.setAmount(amount);
        debito.setType(TransactionType.DEBIT);
        debito.setDescription("Pix enviado para " + sourceUser.getName());
        debito.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(debito);

        // 3. cria transação de CRÉDITO (destino)
        Transaction credito = new Transaction();
        credito.setUserId(targetUser.getId());
        credito.setAmount(amount);
        credito.setType(TransactionType.CREDIT);
        credito.setDescription("Pix recebido do " + targetUser.getName());
        credito.setCreatedAt(LocalDateTime.now());
        transactionRepo.save(credito);

        // 4. atualiza os saldos na tabela Account
        /*
         * accountRepo.findById(userId).ifPresent(account -> {
         * Double current = account.getBalance() != null ? account.getBalance() : 0.0;
         * account.setBalance(current - amount);
         * accountRepo.save(account);
         * });
         * 
         * accountRepo.findById(pixKey).ifPresent(account -> {
         * Double current = account.getBalance() != null ? account.getBalance() : 0.0;
         * account.setBalance(current + amount);
         * accountRepo.save(account);
         * });
         */

    }

    @Override
    public List<Transaction> listTransactions(Long userId) {
        return transactionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
