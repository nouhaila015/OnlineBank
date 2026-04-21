package com.online.banking.controller;

import com.online.banking.config.AppConfig;
import com.online.banking.config.SecurityConfig;
import com.online.banking.entity.User;
import com.online.banking.entity.enums.AccountType;
import com.online.banking.service.BankAccountService;
import com.online.banking.service.UserService;
import com.online.banking.service.exception.BankAccountException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BankAccountController.class)
@Import({SecurityConfig.class, AppConfig.class})
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankAccountService bankAccountService;

    @MockitoBean
    private UserService userService;

    // --- GET /accounts/open ---

    @Test
    void GET_open_shouldReturnOpenView_withAccountTypes() throws Exception {
        mockMvc.perform(get("/accounts/open").with(user("test@example.com")))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/open"))
                .andExpect(model().attributeExists("accountTypes"));
    }

    // --- POST /accounts/open ---

    @Test
    void POST_open_shouldRedirectToDashboard_onSuccess() throws Exception {
        User mockUser = User.builder().id("user-1").email("test@example.com").bankAccounts(new ArrayList<>()).build();
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        doNothing().when(bankAccountService).openBankAccount(any(), eq("user-1"));

        mockMvc.perform(post("/accounts/open").with(csrf()).with(user("test@example.com"))
                        .param("accountType", AccountType.CURRENT.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void POST_open_shouldReturnOpenView_withError_onException() throws Exception {
        User mockUser = User.builder().id("user-1").email("test@example.com").bankAccounts(new ArrayList<>()).build();
        when(userService.findByEmail("test@example.com")).thenReturn(mockUser);
        doThrow(new BankAccountException(BankAccountException.BankAccountError.BANK_ACCOUNT_ALREADY_EXISTS))
                .when(bankAccountService).openBankAccount(any(), anyString());

        mockMvc.perform(post("/accounts/open").with(csrf()).with(user("test@example.com"))
                        .param("accountType", AccountType.SAVING.name()))
                .andExpect(status().isOk())
                .andExpect(view().name("accounts/open"))
                .andExpect(model().attributeExists("error"));
    }

    // --- POST /accounts/{id}/deposit ---

    @Test
    void POST_deposit_shouldRedirectToDashboard_onSuccess() throws Exception {
        doNothing().when(bankAccountService).deposit(eq("acc-1"), any());

        mockMvc.perform(post("/accounts/acc-1/deposit").with(csrf()).with(user("test@example.com"))
                        .param("amount", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void POST_deposit_shouldRedirectToDashboard_withFlashError_onInvalidAmount() throws Exception {
        doThrow(new BankAccountException(BankAccountException.BankAccountError.INVALID_AMOUNT))
                .when(bankAccountService).deposit(eq("acc-1"), any());

        mockMvc.perform(post("/accounts/acc-1/deposit").with(csrf()).with(user("test@example.com"))
                        .param("amount", "0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("error"));
    }

    // --- POST /accounts/{id}/withdraw ---

    @Test
    void POST_withdraw_shouldRedirectToDashboard_onSuccess() throws Exception {
        doNothing().when(bankAccountService).withdraw(eq("acc-1"), any());

        mockMvc.perform(post("/accounts/acc-1/withdraw").with(csrf()).with(user("test@example.com"))
                        .param("amount", "50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void POST_withdraw_shouldRedirectToDashboard_withFlashError_onInsufficientBalance() throws Exception {
        doThrow(new BankAccountException(BankAccountException.BankAccountError.INSUFFICIENT_BALANCE))
                .when(bankAccountService).withdraw(eq("acc-1"), any());

        mockMvc.perform(post("/accounts/acc-1/withdraw").with(csrf()).with(user("test@example.com"))
                        .param("amount", "9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("error"));
    }

    // --- POST /accounts/transfer ---

    @Test
    void POST_transfer_shouldRedirectToDashboard_onSuccess() throws Exception {
        doNothing().when(bankAccountService).transfer(eq("acc-1"), eq("acc-2"), any());

        mockMvc.perform(post("/accounts/transfer").with(csrf()).with(user("test@example.com"))
                        .param("fromAccountId", "acc-1")
                        .param("toAccountId", "acc-2")
                        .param("amount", "200"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void POST_transfer_shouldRedirectToDashboard_withFlashError_onInsufficientBalance() throws Exception {
        doThrow(new BankAccountException(BankAccountException.BankAccountError.INSUFFICIENT_BALANCE))
                .when(bankAccountService).transfer(eq("acc-1"), eq("acc-2"), any());

        mockMvc.perform(post("/accounts/transfer").with(csrf()).with(user("test@example.com"))
                        .param("fromAccountId", "acc-1")
                        .param("toAccountId", "acc-2")
                        .param("amount", "9999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(flash().attributeExists("error"));
    }
}
