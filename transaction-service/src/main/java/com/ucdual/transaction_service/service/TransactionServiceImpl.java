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

    // Realizar transferência entre usuários
    @Override
    @Transactional
    public void transfer(TransferRequest request) {

        Long senderId = request.getUserId();
        String targetPixKey = request.getPixKey();
        Double amount = request.getAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser maior que zero.");
        }

        // verifica usuário de origem
        User sender = userRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário de origem não encontrado."));

        // procura usuário destino pela chave pix (email)
        User receiver = userRepo.findAll().stream()
                .filter(user -> user.getEmail().equals(targetPixKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chave Pix de destino não encontrada."));

        // valida saldo atual da conta do remetente
        Double saldo = getBalance(senderId);
        if (saldo < amount) {
            throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        }

        // cria transação pendente aguardando confirmação do recebedor
        Transaction tx = new Transaction();
        tx.setSenderId(senderId);
        tx.setUserId(receiver.getId()); // dono da transação é o recebedor
        tx.setAmount(amount);
        tx.setType(TransactionType.CREDIT); // será crédito APÓS aceitação
        tx.setStatus(TransactionStatus.PENDING);
        tx.setDescription("Pix pendente de " + sender.getName());
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepo.save(tx);
    }

    @Override
    public List<Transaction> listTransactions(Long userId) {
        return transactionRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // Confirmar ou rejeitar transação pendente
    @Override
    @Transactional
    public void confirmTransaction(Long id, boolean accepted) {
        Transaction tx = transactionRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada"));

        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transação já foi analisada");
        }

        if (accepted) {
            tx.setStatus(TransactionStatus.APPROVED);

            accountRepo.findById(tx.getUserId()).ifPresent(acc -> {
                acc.setBalance(acc.getBalance() + tx.getAmount());
                accountRepo.save(acc);
            });

            accountRepo.findById(tx.getSenderId()).ifPresent(acc -> {
                acc.setBalance(acc.getBalance() - tx.getAmount());
                accountRepo.save(acc);
            });

        } else {
            tx.setStatus(TransactionStatus.REJECTED);
        }

        transactionRepo.save(tx);
    }

    // Listar transações pendentes
    @Override
    public List<Transaction> listPendingTransactions(Long userId) {
        return transactionRepo.findByUserIdAndStatusOrderByCreatedAtDesc(
                userId,
                TransactionStatus.PENDING);
    }

}
