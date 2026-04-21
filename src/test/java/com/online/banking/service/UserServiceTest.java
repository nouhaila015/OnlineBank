package com.online.banking.service;

import com.online.banking.entity.User;
import com.online.banking.entity.enums.Status;
import com.online.banking.entity.enums.UserRole;
import com.online.banking.repositories.UserRepository;
import com.online.banking.service.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-1")
                .email("test@example.com")
                .password("plainPassword")
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    // --- registerUser ---

    @Test
    void registerUser_shouldEncodePasswordAndSetRoleAndStatus() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");

        userService.registerUser(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getPassword()).isEqualTo("encodedPassword");
        assertThat(saved.getRole()).isEqualTo(UserRole.CUSTOMER);
        assertThat(saved.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void registerUser_shouldThrow_whenEmailAlreadyExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.registerUser(user))
                .isInstanceOf(UserException.class)
                .hasMessage(UserException.UserError.USER_EMAIL_ALREADY_EXISTS.getMessage());

        verify(userRepository, never()).save(any());
    }

    // --- findByEmail ---

    @Test
    void findByEmail_shouldReturnUser_whenFound() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User result = userService.findByEmail("test@example.com");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findByEmail_shouldThrow_whenNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("unknown@example.com"))
                .isInstanceOf(UserException.class)
                .hasMessage(UserException.UserError.USER_NOT_FOUND.getMessage());
    }

    // --- findById ---

    @Test
    void findById_shouldReturnUser_whenFound() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        User result = userService.findById("user-1");

        assertThat(result).isEqualTo(user);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById("missing"))
                .isInstanceOf(UserException.class)
                .hasMessage(UserException.UserError.USER_NOT_FOUND.getMessage());
    }

    // --- deleteUser ---

    @Test
    void deleteUser_shouldDeleteUser_whenFound() {
        when(userRepository.findById("user-1")).thenReturn(Optional.of(user));

        userService.deleteUser("user-1");

        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrow_whenNotFound() {
        when(userRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser("missing"))
                .isInstanceOf(UserException.class)
                .hasMessage(UserException.UserError.USER_NOT_FOUND.getMessage());

        verify(userRepository, never()).delete(any());
    }

    // --- loadUserByUsername ---

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenEmailFound() {
        user.setPassword("encodedPassword");
        user.setRole(UserRole.CUSTOMER);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("test@example.com");

        assertThat(details.getUsername()).isEqualTo("test@example.com");
        assertThat(details.getPassword()).isEqualTo("encodedPassword");
        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
    }

    @Test
    void loadUserByUsername_shouldThrow_whenEmailNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
