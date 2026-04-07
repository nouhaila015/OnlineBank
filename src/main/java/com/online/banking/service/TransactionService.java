package com.online.banking.service;

import com.online.banking.entity.BankAccount;
import com.online.banking.entity.Transaction;
import com.online.banking.repositories.TransactionRepository;
import com.online.banking.service.exception.TransactionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public void recordTransaction(Transaction transaction) {
        log.info("Recording transaction of amount {}", transaction.getAmount());
        transactionRepository.save(transaction);
    }

    public List<Transaction> getAccountTransactions(BankAccount bankAccount) {
        List<Transaction> sent = transactionRepository.findByFromBankAccount(bankAccount);
        List<Transaction> received = transactionRepository.findByToBankAccount(bankAccount);
        return Stream.concat(sent.stream(), received.stream()).toList();
    }

    public Transaction getTransactionById(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionException(TransactionException.TransactionError.TRANSACTION_NOT_FOUND));
    }
}
