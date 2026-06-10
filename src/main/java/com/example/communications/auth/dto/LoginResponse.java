package com.example.communications.auth.dto;

public record LoginResponse(
        String token,
        UserResponse user
) {
}