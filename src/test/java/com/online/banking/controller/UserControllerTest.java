package com.online.banking.controller;

import com.online.banking.config.AppConfig;
import com.online.banking.config.SecurityConfig;
import com.online.banking.service.UserService;
import com.online.banking.service.exception.UserException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, AppConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void GET_register_shouldReturnRegisterView() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    void GET_login_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void POST_register_shouldRedirectToLogin_onSuccess() throws Exception {
        doNothing().when(userService).registerUser(any());

        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "test@example.com")
                        .param("password", "secret")
                        .param("firstName", "John")
                        .param("lastName", "Doe"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void POST_register_shouldReturnRegisterView_withError_onDuplicateEmail() throws Exception {
        doThrow(new UserException(UserException.UserError.USER_EMAIL_ALREADY_EXISTS))
                .when(userService).registerUser(any());

        mockMvc.perform(post("/register").with(csrf())
                        .param("email", "existing@example.com")
                        .param("password", "secret"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("error"));
    }
}
