package com.ucdual.transaction_service.service;

import com.ucdual.transaction_service.dto.DepositRequest;
import com.ucdual.transaction_service.dto.TransferRequest;
import com.ucdual.transaction_service.model.Account;
import com.ucdual.transaction_service.model.Transaction;
import com.ucdual.transaction_service.repository.AccountRepository;
import com.ucdual.transaction_service.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepo;
    private final TransactionRepository transactionRepo;

    @Override
    public Double getBalance(Long userId) {
        return accountRepo.findById(userId)
                .map(Account::getBalance)
                .orElse(0.0);
    }

    @Override
    public String deposit(DepositRequest req) {

        Account acc = accountRepo.findById(req.getUserId())
                .orElseGet(() -> {
                    Account newAcc = new Account();
                    newAcc.setUserId(req.getUserId());
                    newAcc.setBalance(0.0);
                    return newAcc;
                });

        acc.setBalance(acc.getBalance() + req.getAmount());
        accountRepo.save(acc);

        Transaction t = new Transaction();
        t.setUserId(req.getUserId());
        t.setAmount(req.getAmount());
        t.setType("CREDIT");
        t.setDescription("Depósito");
        transactionRepo.save(t);

        return "Depósito realizado!";
    }

    @Override
    public String transfer(TransferRequest req) {

        // Conta remetente
        Account from = accountRepo.findById(req.getFromUserId())
                .orElse(null);

        if (from == null) {
            return "Conta remetente não existe!";
        }

        if (from.getBalance() < req.getAmount()) {
            return "Saldo insuficiente!";
        }

        // Conta destino
        Account to = accountRepo.findById(req.getToUserId())
                .orElseGet(() -> {
                    Account newAcc = new Account();
                    newAcc.setUserId(req.getToUserId());
                    newAcc.setBalance(0.0);
                    return newAcc;
                });

        // Débito do remetente
        from.setBalance(from.getBalance() - req.getAmount());
        accountRepo.save(from);

        Transaction debit = new Transaction();
        debit.setUserId(req.getFromUserId());
        debit.setAmount(req.getAmount());
        debit.setType("DEBIT");
        debit.setDescription(req.getDescription());
        transactionRepo.save(debit);

        // Crédito no destinatário
        to.setBalance(to.getBalance() + req.getAmount());
        accountRepo.save(to);

        Transaction credit = new Transaction();
        credit.setUserId(req.getToUserId());
        credit.setAmount(req.getAmount());
        credit.setType("CREDIT");
        credit.setDescription(req.getDescription());
        transactionRepo.save(credit);

        return "Transferência realizada!";
    }

    @Override
    public List<Transaction> listTransactions(Long userId) {
        return transactionRepo.findByUserId(userId);
    }
}
