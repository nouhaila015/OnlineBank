package com.online.banking.service;

import com.online.banking.entity.CurrentAccount;
import com.online.banking.entity.Transaction;
import com.online.banking.entity.enums.ProcessingMode;
import com.online.banking.entity.enums.Status;
import com.online.banking.entity.enums.TransactionType;
import com.online.banking.repositories.TransactionRepository;
import com.online.banking.service.exception.TransactionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private CurrentAccount account;
    private Transaction tx;

    @BeforeEach
    void setUp() {
        account = CurrentAccount.builder().id("acc-1").balance(new BigDecimal("300")).build();

        tx = Transaction.builder()
                .id("tx-1")
                .amount(new BigDecimal("100"))
                .date(LocalDateTime.now())
                .status(Status.ACTIVE)
                .type(TransactionType.CREDIT)
                .processingMode(ProcessingMode.INSTANT)
                .toBankAccount(account)
                .build();
    }

    // --- recordTransaction ---

    @Test
    void recordTransaction_shouldSaveTransaction() {
        transactionService.recordTransaction(tx);

        verify(transactionRepository).save(tx);
    }

    // --- getAccountTransactions ---

    @Test
    void getAccountTransactions_shouldReturnSentAndReceivedTransactions() {
        Transaction sent = Transaction.builder().id("tx-2").amount(new BigDecimal("50"))
                .date(LocalDateTime.now()).fromBankAccount(account).build();
        Transaction received = Transaction.builder().id("tx-3").amount(new BigDecimal("75"))
                .date(LocalDateTime.now()).toBankAccount(account).build();

        when(transactionRepository.findByFromBankAccount(account)).thenReturn(List.of(sent));
        when(transactionRepository.findByToBankAccount(account)).thenReturn(List.of(received));

        List<Transaction> result = transactionService.getAccountTransactions(account);

        assertThat(result).containsExactlyInAnyOrder(sent, received);
    }

    @Test
    void getAccountTransactions_shouldReturnEmptyList_whenNoTransactions() {
        when(transactionRepository.findByFromBankAccount(account)).thenReturn(List.of());
        when(transactionRepository.findByToBankAccount(account)).thenReturn(List.of());

        assertThat(transactionService.getAccountTransactions(account)).isEmpty();
    }

    // --- getTransactionById ---

    @Test
    void getTransactionById_shouldReturnTransaction_whenFound() {
        when(transactionRepository.findById("tx-1")).thenReturn(Optional.of(tx));

        assertThat(transactionService.getTransactionById("tx-1")).isEqualTo(tx);
    }

    @Test
    void getTransactionById_shouldThrow_whenNotFound() {
        when(transactionRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionService.getTransactionById("missing"))
                .isInstanceOf(TransactionException.class)
                .hasMessage(TransactionException.TransactionError.TRANSACTION_NOT_FOUND.getMessage());
    }
}
