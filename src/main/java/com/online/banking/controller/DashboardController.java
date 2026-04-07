package com.online.banking.controller;

import com.online.banking.entity.BankAccount;
import com.online.banking.entity.Transaction;
import com.online.banking.entity.User;
import com.online.banking.service.TransactionService;
import com.online.banking.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController {
    private final UserService userService;
    private final TransactionService transactionService;

    public DashboardController(UserService userService, TransactionService transactionService) {
        this.userService = userService;
        this.transactionService = transactionService;
    }

    @Transactional
    @GetMapping("/dashboard")
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<BankAccount> accounts = user.getBankAccounts();

        List<Transaction> recentTransactions = accounts.stream()
                .flatMap(account -> transactionService.getAccountTransactions(account).stream())
                .sorted(Comparator.comparing(Transaction::getDate).reversed())
                .limit(10)
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("recentTransactions", recentTransactions);
        return "dashboard";
    }
}
