package com.online.banking.controller;

import com.online.banking.config.AppConfig;
import com.online.banking.config.SecurityConfig;
import com.online.banking.entity.CurrentAccount;
import com.online.banking.entity.Transaction;
import com.online.banking.entity.User;
import com.online.banking.entity.enums.AccountType;
import com.online.banking.entity.enums.Status;
import com.online.banking.service.TransactionService;
import com.online.banking.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, AppConfig.class})
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void GET_dashboard_shouldReturnDashboardView_withModelAttributes() throws Exception {
        CurrentAccount account = CurrentAccount.builder()
                .id("acc-1")
                .balance(new BigDecimal("1000"))
                .openedDate(LocalDate.now())
                .status(Status.ACTIVE)
                .accountType(AccountType.CURRENT)
                .build();

        User mockUser = User.builder()
                .id("user-1")
                .email("test@example.com")
                .firstName("John")
                .bankAccounts(List.of(account))
                .build();

        Transaction tx = Transaction.builder()
                .id("tx-1")
                .amount(new BigDecimal("50"))
                .date(LocalDateTime.now())
                .build();

        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        when(transactionService.getAccountTransactions(any())).thenReturn(List.of(tx));

        mockMvc.perform(get("/dashboard").with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("user", "accounts", "recentTransactions"));
    }

    @Test
    void GET_dashboard_shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection());
    }
}
