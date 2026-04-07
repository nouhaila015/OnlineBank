package com.online.banking.service;

import com.online.banking.entity.BankAccount;
import com.online.banking.entity.Transaction;
import com.online.banking.entity.User;
import com.online.banking.entity.enums.ProcessingMode;
import com.online.banking.entity.enums.Status;
import com.online.banking.entity.enums.TransactionType;
import com.online.banking.repositories.BankAccountRepository;
import com.online.banking.service.exception.BankAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class BankAccountService {
    private static final Logger log = LoggerFactory.getLogger(BankAccountService.class);
    private final BankAccountRepository bankAccountRepository;
    private final UserService userService;
    private final TransactionService transactionService;

    public BankAccountService(BankAccountRepository bankAccountRepository,
                              @Lazy UserService userService,
                              TransactionService transactionService) {
        this.bankAccountRepository = bankAccountRepository;
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @Transactional
    public void openBankAccount(BankAccount bankAccount, String userId) {
        User user = userService.findById(userId);
        log.info("Opening bank account for userId: {}", userId);
        bankAccountRepository.save(bankAccount);
        user.getBankAccounts().add(bankAccount);
    }

    public void closeAccount(String bankAccountId) {
        BankAccount account = findByaccountId(bankAccountId);
        log.info("Closing bank account: {}", bankAccountId);
        account.setStatus(Status.INACTIVE);
        bankAccountRepository.save(account);
    }

    @Transactional
    public void deposit(String accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankAccountException(BankAccountException.BankAccountError.INVALID_AMOUNT);
        }
        BankAccount bankAccount = findByaccountId(accountId);
        log.info("Depositing {} to account: {}", amount, accountId);
        bankAccount.setBalance(bankAccount.getBalance().add(amount));
        bankAccountRepository.save(bankAccount);
        transactionService.recordTransaction(Transaction.builder()
                .amount(amount)
                .date(LocalDateTime.now())
                .status(Status.ACTIVE)
                .type(TransactionType.CREDIT)
                .processingMode(ProcessingMode.INSTANT)
                .toBankAccount(bankAccount)
                .build());
    }

    @Transactional
    public void withdraw(String accountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankAccountException(BankAccountException.BankAccountError.INVALID_AMOUNT);
        }
        BankAccount bankAccount = findByaccountId(accountId);
        if (bankAccount.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new BankAccountException(BankAccountException.BankAccountError.INSUFFICIENT_BALANCE);
        }
        log.info("Withdrawing {} from account: {}", amount, accountId);
        bankAccount.setBalance(bankAccount.getBalance().subtract(amount));
        bankAccountRepository.save(bankAccount);
        transactionService.recordTransaction(Transaction.builder()
                .amount(amount)
                .date(LocalDateTime.now())
                .status(Status.ACTIVE)
                .type(TransactionType.DEBIT)
                .processingMode(ProcessingMode.INSTANT)
                .fromBankAccount(bankAccount)
                .build());
    }

    @Transactional
    public void transfer(String fromAccountId, String toAccountId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BankAccountException(BankAccountException.BankAccountError.INVALID_AMOUNT);
        }
        BankAccount fromBankAccount = findByaccountId(fromAccountId);
        BankAccount toBankAccount = findByaccountId(toAccountId);
        if (fromBankAccount.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new BankAccountException(BankAccountException.BankAccountError.INSUFFICIENT_BALANCE);
        }
        log.info("Transferring {} from account {} to account {}", amount, fromAccountId, toAccountId);
        fromBankAccount.setBalance(fromBankAccount.getBalance().subtract(amount));
        toBankAccount.setBalance(toBankAccount.getBalance().add(amount));
        bankAccountRepository.save(fromBankAccount);
        bankAccountRepository.save(toBankAccount);
        transactionService.recordTransaction(Transaction.builder()
                .amount(amount)
                .date(LocalDateTime.now())
                .status(Status.ACTIVE)
                .type(TransactionType.DEBIT)
                .processingMode(ProcessingMode.INSTANT)
                .fromBankAccount(fromBankAccount)
                .toBankAccount(toBankAccount)
                .build());
    }

    public BankAccount findByaccountId(String accountId) {
        return bankAccountRepository.findById(accountId)
                .orElseThrow(() -> new BankAccountException(BankAccountException.BankAccountError.BANK_ACCOUNT_NOT_FOUND));
    }
}
