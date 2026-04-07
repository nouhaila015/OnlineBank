package com.online.banking.repositories;

import com.online.banking.entity.BankAccount;
import com.online.banking.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByFromBankAccount(BankAccount fromBankAccount);

    List<Transaction> findByToBankAccount(BankAccount fromBankAccount);
}
