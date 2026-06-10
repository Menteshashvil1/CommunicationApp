package com.example.communications.contact.dto;

import com.example.communications.user.model.User;

public record ContactResponse(
        Long id,
        String email,
        String displayName
) {
    public static ContactResponse from(User user) {
        return new ContactResponse(
                user.getId(),
                user.getEmail(),
                user.getDisplayName()
        );
    }
}
