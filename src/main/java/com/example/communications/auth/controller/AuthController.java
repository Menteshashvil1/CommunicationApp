package com.example.communications.auth.controller;


import com.example.communications.auth.dto.LoginRequest;
import com.example.communications.auth.dto.LoginResponse;
import com.example.communications.auth.dto.RegisterRequest;
import com.example.communications.user.dto.UserResponse;
import com.example.communications.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")

public class AuthController {
   private final AuthService authService;

   public AuthController(AuthService authService) {
       this.authService = authService;
   }
   @PostMapping("/register")
   @ResponseStatus(HttpStatus.CREATED)

   public UserResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
       return authService.register(registerRequest);
   }
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }
}
