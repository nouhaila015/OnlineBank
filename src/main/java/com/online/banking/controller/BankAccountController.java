package com.online.banking.controller;

import com.online.banking.entity.BankAccount;
import com.online.banking.entity.CreditAccount;
import com.online.banking.entity.CurrentAccount;
import com.online.banking.entity.FixedDepositAccount;
import com.online.banking.entity.SavingAccount;
import com.online.banking.entity.enums.AccountType;
import com.online.banking.entity.enums.Status;
import com.online.banking.service.BankAccountService;
import com.online.banking.service.UserService;
import com.online.banking.service.exception.BankAccountException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/accounts")
public class BankAccountController {
    private final BankAccountService bankAccountService;
    private final UserService userService;

    public BankAccountController(BankAccountService bankAccountService, UserService userService) {
        this.bankAccountService = bankAccountService;
        this.userService = userService;
    }

    @GetMapping("/open")
    public String showOpenAccountPage(Model model) {
        model.addAttribute("accountTypes", AccountType.values());
        return "accounts/open";
    }

    @PostMapping("/open")
    public String handleOpenAccount(@AuthenticationPrincipal UserDetails userDetails,
                                    @RequestParam AccountType accountType,
                                    Model model) {
        try {
            String userId = userService.findByEmail(userDetails.getUsername()).getId();
            BankAccount bankAccount = buildAccount(accountType);
            bankAccountService.openBankAccount(bankAccount, userId);
            return "redirect:/dashboard";
        } catch (BankAccountException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("accountTypes", AccountType.values());
            return "accounts/open";
        }
    }

    @PostMapping("/{id}/deposit")
    public String handleDeposit(@PathVariable String id,
                                @RequestParam BigDecimal amount,
                                RedirectAttributes redirectAttributes) {
        try {
            bankAccountService.deposit(id, amount);
            return "redirect:/dashboard";
        } catch (BankAccountException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/{id}/withdraw")
    public String handleWithdraw(@PathVariable String id,
                                 @RequestParam BigDecimal amount,
                                 RedirectAttributes redirectAttributes) {
        try {
            bankAccountService.withdraw(id, amount);
            return "redirect:/dashboard";
        } catch (BankAccountException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/transfer")
    public String handleTransfer(@RequestParam String fromAccountId,
                                 @RequestParam String toAccountId,
                                 @RequestParam BigDecimal amount,
                                 RedirectAttributes redirectAttributes) {
        try {
            bankAccountService.transfer(fromAccountId, toAccountId, amount);
            return "redirect:/dashboard";
        } catch (BankAccountException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/dashboard";
        }
    }

    private BankAccount buildAccount(AccountType accountType) {
        LocalDate today = LocalDate.now();
        return switch (accountType) {
            case CURRENT -> CurrentAccount.builder()
                    .balance(BigDecimal.ZERO)
                    .openedDate(today)
                    .status(Status.ACTIVE)
                    .accountType(AccountType.CURRENT)
                    .build();
            case SAVING -> SavingAccount.builder()
                    .accountType(AccountType.SAVING)
                    .balance(BigDecimal.ZERO)
                    .openedDate(today)
                    .status(Status.ACTIVE)
                    .build();
            case CREDIT -> CreditAccount.builder()
                    .accountType(AccountType.CREDIT)
                    .balance(BigDecimal.ZERO)
                    .openedDate(today)
                    .status(Status.ACTIVE)
                    .build();
            case FIXED_DEPOSIT -> FixedDepositAccount.builder()
                    .accountType(AccountType.FIXED_DEPOSIT)
                    .balance(BigDecimal.ZERO)
                    .openedDate(today)
                    .status(Status.ACTIVE)
                    .build();
        };
    }
}
