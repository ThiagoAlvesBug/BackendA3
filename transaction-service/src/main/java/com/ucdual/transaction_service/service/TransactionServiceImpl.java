package com.ucdual.transaction_service.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Account;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.model.TransactionStatus;
import com.ucdual.transaction_service.model.User;
import com.ucdual.transaction_service.repository.AccountRepository;
import com.ucdual.transaction_service.repository.TransactionRepository;
import com.ucdual.transaction_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;

// Implementação do serviço de transações financeiras
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;
    private final AccountRepository accountRepo;

    @Override
    public Double getBalance(Long userId) {
        return accountRepo.findById(userId)
                .map(Account::getBalance)
                .orElse(0.0);
    }
    // Implementação do depósito
    @Override
    @Transactional
    public void deposit(DepositRequest request) {
        Long userId = request.getUserId();
        Double amount = request.getAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Valor do depósito deve ser maior que zero.");
        }

        Account account = accountRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));

        account.setBalance(account.getBalance() + amount);
        accountRepo.save(account);

        Transaction tx = new Transaction();
        tx.setSenderId(null);
        tx.setReceiverId(userId);
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.CONFIRMED);
        tx.setDescription("Depósito");
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepo.save(tx);
    }
    // Implementação da transferência
    @Override
    @Transactional
    public void transfer(TransferRequest request) {
        Long senderId = request.getUserId();
        String targetPixKey = request.getPixKey();
        Double amount = request.getAmount();

        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Valor da transferência deve ser maior que zero.");
        }

        // Valida existência do remetente
        Account senderAccount = accountRepo.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("Conta do remetente não encontrada"));

        // Busca destinatário pela chave Pix (email)
        User receiver = userRepo.findAll().stream()
                .filter(user -> user.getEmail().equals(targetPixKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chave Pix de destino não encontrada"));

        // Verifica saldo
        if (senderAccount.getBalance() < amount) {
            throw new IllegalArgumentException("Saldo insuficiente para transferência.");
        }

        Transaction tx = new Transaction();
        tx.setSenderId(senderId);
        tx.setReceiverId(receiver.getId());
        tx.setAmount(amount);
        tx.setStatus(TransactionStatus.PENDING);
        tx.setDescription(request.getDescription() != null ? request.getDescription() : "Pix pendente");
        tx.setCreatedAt(LocalDateTime.now());

        transactionRepo.save(tx);
    }
    // Listagem de transações do usuário
    @Override
    public List<Transaction> listTransactions(Long userId) {
        return transactionRepo.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId);
    }
    // Listagem de transações pendentes do usuário
    @Override
    public List<Transaction> listPendingTransactions(Long userId) {
        return transactionRepo.findByReceiverIdAndStatusOrderByCreatedAtDesc(userId, TransactionStatus.PENDING);
    }
    // Confirmação ou rejeição de transação pendente
    @Override
    @Transactional
    public void confirmTransaction(Long userId, Long transactionId, boolean accepted) {
        Transaction tx = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada"));

        if (tx.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transação já foi confirmada ou rejeitada");
        }

        if (accepted) {
            // Credita receptor e debita remetente
            Account receiverAccount = accountRepo.findById(tx.getReceiverId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta do destinatário não encontrada"));

            Account senderAccount = accountRepo.findById(tx.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("Conta do remetente não encontrada"));

            senderAccount.setBalance(senderAccount.getBalance() - tx.getAmount());
            receiverAccount.setBalance(receiverAccount.getBalance() + tx.getAmount());

            accountRepo.save(senderAccount);
            accountRepo.save(receiverAccount);

            tx.setStatus(TransactionStatus.CONFIRMED);
        } else {
            tx.setStatus(TransactionStatus.REJECTED);
        }

        transactionRepo.save(tx);
    }
}
