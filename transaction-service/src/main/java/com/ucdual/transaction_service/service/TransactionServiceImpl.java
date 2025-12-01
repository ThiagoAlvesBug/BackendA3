package com.ucdual.transaction_service.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.model.TransactionStatus;
import com.ucdual.transaction_service.model.TransactionWithUsers;
import com.ucdual.transaction_service.model.User;
import com.ucdual.transaction_service.repository.TransactionRepository;
import com.ucdual.transaction_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;

// Implementação do serviço de transações financeiras
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepo;
    private final UserRepository userRepo;
    // private final AccountRepository accountRepo;

    @Override

    public Double getBalance(Long userId) {
        // Valida existência do remetente
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário remetente não encontrado"));

        List<Transaction> confirmedTransactions = transactionRepo
                .findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.CONFIRMED)
                .toList();

        double totalCredit = confirmedTransactions.stream()
                .filter(tran -> userId != null && userId.equals(tran.getReceiverId()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalDebit = confirmedTransactions.stream()
                .filter(tran -> userId != null && userId.equals(tran.getSenderId()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        return user.getInitialBalance() + totalCredit - totalDebit;
    }

    // Implementação do depósito
    /*
     * @Override
     * 
     * @Transactional
     * public void deposit(DepositRequest request) {
     * Long userId = request.getUserId();
     * Double amount = request.getAmount();
     * 
     * if (amount == null || amount <= 0) {
     * throw new
     * IllegalArgumentException("Valor do depósito deve ser maior que zero.");
     * }
     * 
     * Account account = accountRepo.findById(userId)
     * .orElseThrow(() -> new IllegalArgumentException("Conta não encontrada"));
     * 
     * account.setBalance(account.getBalance() + amount);
     * accountRepo.save(account);
     * 
     * Transaction tx = new Transaction();
     * tx.setSenderId(null);
     * tx.setReceiverId(userId);
     * tx.setAmount(amount);
     * tx.setStatus(TransactionStatus.CONFIRMED);
     * tx.setDescription("Depósito");
     * tx.setCreatedAt(LocalDateTime.now());
     * 
     * transactionRepo.save(tx);
     * }
     */

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
        /*
         * Account senderAccount = accountRepo.findById(senderId)
         * .orElseThrow(() -> new
         * IllegalArgumentException("Conta do remetente não encontrada"));
         */

        // Busca destinatário pela chave Pix (email)
        User receiver = userRepo.findAll().stream()
                .filter(user -> user.getEmail().equals(targetPixKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Chave Pix de destino não encontrada"));

        // Verifica saldo
        if (getBalance(senderId) < amount) {
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
    public List<TransactionWithUsers> listPendingTransactions(Long userId) {
        return transactionRepo
                .findByReceiverIdAndStatusOrderByCreatedAtDesc(userId, TransactionStatus.PENDING)
                .stream()
                .map(tx -> {
                    TransactionWithUsers txWithUsers = new TransactionWithUsers();
                    txWithUsers.setId(tx.getId());
                    txWithUsers.setSenderId(tx.getSenderId());
                    txWithUsers.setReceiverId(tx.getReceiverId());
                    txWithUsers.setAmount(tx.getAmount());
                    txWithUsers.setStatus(tx.getStatus());
                    txWithUsers.setDescription(tx.getDescription());
                    txWithUsers.setCreatedAt(tx.getCreatedAt());

                    User sender = userRepo.findById(tx.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("Usuário remetente não encontrado"));
                    User receiver = userRepo.findById(tx.getReceiverId())
                            .orElseThrow(() -> new IllegalArgumentException("Usuário destinatário não encontrado"));

                    txWithUsers.setSender(sender);
                    txWithUsers.setReceiver(receiver);

                    return txWithUsers;
                }).toList();
    }

    // Listagem de transações confirmadas do usuário
    @Override
    public List<TransactionWithUsers> listConfirmedTransactions(Long userId) {
        return transactionRepo
                .findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId)
                .stream()
                .filter(transaction -> transaction.getStatus() == TransactionStatus.CONFIRMED)
                .map(tx -> {
                    TransactionWithUsers txWithUsers = new TransactionWithUsers();
                    txWithUsers.setId(tx.getId());
                    txWithUsers.setSenderId(tx.getSenderId());
                    txWithUsers.setReceiverId(tx.getReceiverId());
                    txWithUsers.setAmount(tx.getAmount());
                    txWithUsers.setStatus(tx.getStatus());
                    txWithUsers.setDescription(tx.getDescription());
                    txWithUsers.setCreatedAt(tx.getCreatedAt());

                    User sender = userRepo.findById(tx.getSenderId())
                            .orElseThrow(() -> new IllegalArgumentException("Usuário remetente não encontrado"));
                    User receiver = userRepo.findById(tx.getReceiverId())
                            .orElseThrow(() -> new IllegalArgumentException("Usuário destinatário não encontrado"));

                    txWithUsers.setSender(sender);
                    txWithUsers.setReceiver(receiver);

                    return txWithUsers;
                }).toList();
    }

    // Confirmação ou rejeição de transação pendente
    @Override
    @Transactional
    public void confirmTransaction(Long userId, Long transactionId, boolean accepted) {
        Transaction transaction = transactionRepo.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transação não encontrada"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new IllegalStateException("Transação já foi confirmada ou rejeitada");
        }

        if (accepted) {
            // Credita receptor e debita remetente
            /*
             * Account receiverAccount = accountRepo.findById(transaction.getReceiverId())
             * .orElseThrow(() -> new
             * IllegalArgumentException("Conta do destinatário não encontrada"));
             * 
             * Account senderAccount = accountRepo.findById(transaction.getSenderId())
             * .orElseThrow(() -> new
             * IllegalArgumentException("Conta do remetente não encontrada"));
             * 
             * senderAccount.setBalance(senderAccount.getBalance() -
             * transaction.getAmount());
             * receiverAccount.setBalance(receiverAccount.getBalance() +
             * transaction.getAmount());
             * 
             * accountRepo.save(senderAccount);
             * accountRepo.save(receiverAccount);
             */

            transaction.setStatus(TransactionStatus.CONFIRMED);
        } else {
            transaction.setStatus(TransactionStatus.REJECTED);
        }

        transactionRepo.save(transaction);
    }
}
