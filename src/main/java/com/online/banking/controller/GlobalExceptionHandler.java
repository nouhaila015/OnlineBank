package com.online.banking.controller;

import com.online.banking.service.exception.UserException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserException.class)
    public String handleUserNotFound(HttpServletRequest request) {
        request.getSession().invalidate();
        return "redirect:/login";
    }
}
