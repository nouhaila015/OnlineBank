package com.online.banking.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        if (statusCode == null || statusCode == 404) {
            return userDetails != null ? "redirect:/dashboard" : "redirect:/login";
        }
        model.addAttribute("status", statusCode);
        String message = (String) request.getAttribute("jakarta.servlet.error.message");
        model.addAttribute("error", message != null ? message : "Internal Server Error");
        return "error";
    }
}
