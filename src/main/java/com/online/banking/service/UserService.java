package com.online.banking.service;

import com.online.banking.entity.User;
import com.online.banking.entity.enums.Status;
import com.online.banking.entity.enums.UserRole;
import com.online.banking.repositories.UserRepository;
import com.online.banking.service.exception.UserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) {
        // username in our case is the email
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserException(UserException.UserError.USER_EMAIL_ALREADY_EXISTS);
        }
        log.info("Registering user with email: {}", user.getEmail());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserException.UserError.USER_NOT_FOUND));
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserException.UserError.USER_NOT_FOUND));
    }

    public void updateUser(User user) {
        userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new UserException(UserException.UserError.USER_NOT_FOUND));
        log.info("Updating user with email: {}", user.getEmail());
        userRepository.save(user);
    }

    public void deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserException(UserException.UserError.USER_NOT_FOUND));
        log.info("Deleting user with id: {}", id);
        userRepository.delete(user);
    }
}
