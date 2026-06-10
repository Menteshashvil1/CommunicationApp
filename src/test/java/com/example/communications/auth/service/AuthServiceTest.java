package com.example.communications.auth.service;

import com.example.communications.auth.dto.RegisterRequest;
import com.example.communications.auth.dto.UserResponse;
import com.example.communications.auth.exception.EmailAlreadyRegisteredException;
import com.example.communications.user.model.User;
import com.example.communications.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final AuthService authService = new AuthService(userRepository, passwordEncoder);

    @Test
    void registerCreatesUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest(
                "Student@Example.com",
                "StrongPassword123!",
                "Student"
        );

        when(userRepository.existsByEmail("student@example.com")).thenReturn(false);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        UserResponse response = authService.register(request);

        assertEquals(1L, response.id());
        assertEquals("student@example.com", response.email());
        assertEquals("Student", response.displayName());

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("student@example.com")
                        && passwordEncoder.matches("StrongPassword123!", user.getPasswordHash())
                        && !user.getPasswordHash().equals("StrongPassword123!")
        ));
    }

    @Test
    void registerRejectsDuplicateEmail() {
        RegisterRequest request = new RegisterRequest(
                "student@example.com",
                "StrongPassword123!",
                "Student"
        );

        when(userRepository.existsByEmail("student@example.com")).thenReturn(true);

        assertThrows(
                EmailAlreadyRegisteredException.class,
                () -> authService.register(request)
        );

        verify(userRepository, never()).save(any(User.class));
    }
}