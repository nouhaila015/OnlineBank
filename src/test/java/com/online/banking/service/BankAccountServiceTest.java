package com.online.banking.service;

import com.online.banking.entity.CurrentAccount;
import com.online.banking.entity.Transaction;
import com.online.banking.entity.User;
import com.online.banking.entity.enums.AccountType;
import com.online.banking.entity.enums.Status;
import com.online.banking.entity.enums.TransactionType;
import com.online.banking.repositories.BankAccountRepository;
import com.online.banking.service.exception.BankAccountException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UserService userService;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private BankAccountService bankAccountService;

    private CurrentAccount account;

    @BeforeEach
    void setUp() {
        account = CurrentAccount.builder()
                .id("acc-1")
                .balance(new BigDecimal("500.00"))
                .openedDate(LocalDate.now())
                .status(Status.ACTIVE)
                .accountType(AccountType.CURRENT)
                .build();
    }

    // --- findByaccountId ---

    @Test
    void findByaccountId_shouldReturnAccount_whenFound() {
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));

        assertThat(bankAccountService.findByaccountId("acc-1")).isEqualTo(account);
    }

    @Test
    void findByaccountId_shouldThrow_whenNotFound() {
        when(bankAccountRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.findByaccountId("missing"))
                .isInstanceOf(BankAccountException.class)
                .hasMessage(BankAccountException.BankAccountError.BANK_ACCOUNT_NOT_FOUND.getMessage());
    }

    // --- openBankAccount ---

    @Test
    void openBankAccount_shouldSaveAccountAndLinkToUser() {
        User user = User.builder()
                .id("user-1")
                .email("a@b.com")
                .bankAccounts(new ArrayList<>())
                .build();
        when(userService.findById("user-1")).thenReturn(user);

        bankAccountService.openBankAccount(account, "user-1");

        verify(bankAccountRepository).save(account);
        assertThat(user.getBankAccounts()).contains(account);
    }

    // --- closeAccount ---

    @Test
    void closeAccount_shouldSetStatusInactive() {
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));

        bankAccountService.closeAccount("acc-1");

        assertThat(account.getStatus()).isEqualTo(Status.INACTIVE);
        verify(bankAccountRepository).save(account);
    }

    @Test
    void closeAccount_shouldThrow_whenAccountNotFound() {
        when(bankAccountRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.closeAccount("missing"))
                .isInstanceOf(BankAccountException.class)
                .hasMessage(BankAccountException.BankAccountError.BANK_ACCOUNT_NOT_FOUND.getMessage());
    }

    // --- deposit ---

    @Test
    void deposit_shouldIncreaseBalance_andRecordCreditTransaction() {
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));

        bankAccountService.deposit("acc-1", new BigDecimal("200.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("700.00");
        verify(bankAccountRepository).save(account);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionService).recordTransaction(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertThat(tx.getType()).isEqualTo(TransactionType.CREDIT);
        assertThat(tx.getAmount()).isEqualByComparingTo("200.00");
        assertThat(tx.getToBankAccount()).isEqualTo(account);
    }

    @Test
    void deposit_shouldThrow_whenAmountIsZero() {
        assertThatThrownBy(() -> bankAccountService.deposit("acc-1", BigDecimal.ZERO))
                .isInstanceOf(BankAccountException.class)
                .hasMessage(BankAccountException.BankAccountError.INVALID_AMOUNT.getMessage());

        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void deposit_shouldThrow_whenAmountIsNegative() {
        assertThatThrownBy(() -> bankAccountService.deposit("acc-1", new BigDecimal("-50")))
                .isInstanceOf(BankAccountException.class)
                .hasMessage(BankAccountException.BankAccountError.INVALID_AMOUNT.getMessage());
    }

    // --- withdraw ---

    @Test
    void withdraw_shouldDecreaseBalance_andRecordDebitTransaction() {
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));

        bankAccountService.withdraw("acc-1", new BigDecimal("100.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("400.00");
        verify(bankAccountRepository).save(account);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionService).recordTransaction(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertThat(tx.getType()).isEqualTo(TransactionType.DEBIT);
        assertThat(tx.getFromBankAccount()).isEqualTo(account);
    }

    @Test
    void withdraw_shouldThrow_whenInsufficientBalance() {
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> bankAccountService.withdraw("acc-1", new BigDecimal("600.00")))
                .isInstanceOf(BankAccountException.class)
                .hasMessage(BankAccountException.BankAccountError.INSUFFICIENT_BALANCE.getMessage());

        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void withdraw_shouldThrow_whenAmountIsZero() {
        assertThatThrownBy(() -> bankAccountService.withdraw("acc-1", BigDecimal.ZERO))
                .isInstanceOf(BankAccountException.class)
                .hasMessage(BankAccountException.BankAccountError.INVALID_AMOUNT.getMessage());
    }

    @Test
    void withdraw_shouldSucceed_whenAmountEqualsBalance() {
        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));

        bankAccountService.withdraw("acc-1", new BigDecimal("500.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("0.00");
    }

    // --- transfer ---

    @Test
    void transfer_shouldMoveAmountBetweenAccounts_andRecordTransaction() {
        CurrentAccount destination = CurrentAccount.builder()
                .id("acc-2")
                .balance(new BigDecimal("100.00"))
                .status(Status.ACTIVE)
                .accountType(AccountType.CURRENT)
                .overdraftLimit(new BigDecimal("500")) // valeur différente pour que equals() distingue les deux comptes
                .build();

        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));
        when(bankAccountRepository.findById("acc-2")).thenReturn(Optional.of(destination));

        bankAccountService.transfer("acc-1", "acc-2", new BigDecimal("200.00"));

        assertThat(account.getBalance()).isEqualByComparingTo("300.00");
        assertThat(destination.getBalance()).isEqualByComparingTo("300.00");

        verify(bankAccountRepository).save(account);
        verify(bankAccountRepository).save(destination);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionService).recordTransaction(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertThat(tx.getFromBankAccount()).isEqualTo(account);
        assertThat(tx.getToBankAccount()).isEqualTo(destination);
        assertThat(tx.getAmount()).isEqualByComparingTo("200.00");
    }

    @Test
    void transfer_shouldThrow_whenInsufficientBalance() {
        CurrentAccount destination = CurrentAccount.builder()
                .id("acc-2")
                .balance(BigDecimal.ZERO)
                .build();

        when(bankAccountRepository.findById("acc-1")).thenReturn(Optional.of(account));
        when(bankAccountRepository.findById("acc-2")).thenReturn(Optional.of(destination));

        assertThatThrownBy(() -> bankAccountService.transfer("acc-1", "acc-2", new BigDecimal("999.00")))
                .isInstanceOf(BankAccountException.class)
                .hasMessage(BankAccountException.BankAccountError.INSUFFICIENT_BALANCE.getMessage());

        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    void transfer_shouldThrow_whenAmountIsNegative() {
        assertThatThrownBy(() -> bankAccountService.transfer("acc-1", "acc-2", new BigDecimal("-10")))
                .isInstanceOf(BankAccountException.class)
                .hasMessage(BankAccountException.BankAccountError.INVALID_AMOUNT.getMessage());
    }
}
