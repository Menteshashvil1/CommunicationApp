package com.example.communications.auth.dto;

import com.example.communications.user.dto.UserResponse;

public record LoginResponse(
        String token,
        UserResponse user
) {
}