package com.example.communications.auth.service;

import com.example.communications.auth.dto.LoginRequest;
import com.example.communications.auth.dto.LoginResponse;
import com.example.communications.auth.dto.RegisterRequest;
import com.example.communications.user.dto.UserResponse;
import com.example.communications.auth.exception.EmailAlreadyRegisteredException;
import com.example.communications.auth.exception.InvalidCredentialsException;
import com.example.communications.user.model.User;
import com.example.communications.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        String email = registerRequest.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        String passwordHash = passwordEncoder.encode(registerRequest.password());

        User user = new User(
                email,
                passwordHash,
                registerRequest.displayName().trim()
        );

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }


    public LoginResponse login(LoginRequest loginRequest) {
        String email = loginRequest.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches =  passwordEncoder.matches(
                loginRequest.password(),
                user.getPasswordHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }
        String token = jwtService.generateToken(user);

        return new LoginResponse(
                token,
                UserResponse.from(user)
        );
    }
}