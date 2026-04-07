package com.online.banking.controller;

import com.online.banking.entity.User;
import com.online.banking.service.UserService;
import com.online.banking.service.exception.UserException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute User user, Model model) {
        try {
            userService.registerUser(user);
            return "redirect:/login";
        } catch (UserException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginPage(Model model) {
        return "login";
    }
}
